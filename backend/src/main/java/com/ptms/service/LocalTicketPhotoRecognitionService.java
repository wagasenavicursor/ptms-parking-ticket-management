package com.ptms.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.ptms.dto.TicketPhotoRecognitionResponse;
import com.ptms.dto.TicketPhotoRecognitionRow;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalTicketPhotoRecognitionService {
  private static final Pattern BARCODE_TEXT = Pattern.compile("(?<!\\d)(\\d{7,14})(?!\\d)");
  private static final Pattern PHYSICAL_NUMBER = Pattern.compile("(?<!\\d)(\\d{6})(?!\\d)");
  private static final Pattern DURATION = Pattern.compile("valid\\s+for\\s+(\\d{1,2})\\s*hours?", Pattern.CASE_INSENSITIVE);
  private static final Pattern DATE = Pattern.compile("(20\\d{2})\\s*[-/.]\\s*(\\d{1,2})\\s*[-/.]\\s*(\\d{1,2})");
  private static final Pattern SMALL_NUMBER = Pattern.compile("^[1-9]\\d{0,2}$");
  private static final long MAX_IMAGE_BYTES = 12L * 1024 * 1024;

  static { OpenCV.loadLocally(); }

  private final String configuredTessdataPath;

  public LocalTicketPhotoRecognitionService(@Value("${PTMS_TESSDATA_PATH:}") String tessdataPath) {
    this.configuredTessdataPath = tessdataPath == null ? "" : tessdataPath.trim();
  }

  public TicketPhotoRecognitionResponse recognize(MultipartFile image) {
    if (image == null || image.isEmpty()) return failed("Choose a ticket image first.");
    if (image.getSize() > MAX_IMAGE_BYTES) return failed("The image exceeds the 12 MB recognition limit.");
    String contentType = image.getContentType() == null ? "" : image.getContentType();
    if (!contentType.startsWith("image/")) return failed("Only image files can be recognized locally.");
    Mat source = null;
    try {
      source = Imgcodecs.imdecode(new MatOfByte(image.getBytes()), Imgcodecs.IMREAD_COLOR);
      if (source.empty()) return failed("The selected image could not be decoded.");
      List<Rect> ticketBounds = detectTickets(source);
      List<TicketPhotoRecognitionRow> rows = new ArrayList<>();
      for (int index = 0; index < ticketBounds.size(); index++) {
        Rect bounds = ticketBounds.get(index);
        Mat crop = new Mat(source, bounds).clone();
        try {
          TicketPhotoRecognitionRow row = recognizeTicket(crop, index, ticketBounds.size());
          if (hasValue(row)) rows.add(row);
          else rows.add(new TicketPhotoRecognitionRow(null, null, null, null, null, null, 0d));
        } finally { crop.release(); }
      }
      return new TicketPhotoRecognitionResponse(true, "LOCAL_OPENCV_ZXING_TESSERACT",
          "Detected " + rows.size() + " ticket" + (rows.size() == 1 ? "" : "s")
              + " locally. Review all values against the hard copy before saving.", rows);
    } catch (Exception e) {
      return failed("Local ticket recognition could not complete; browser OCR will be used.");
    } finally {
      if (source != null) source.release();
    }
  }

  private List<Rect> detectTickets(Mat source) {
    Mat gray = new Mat(), blurred = new Mat(), edges = new Mat(), hierarchy = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();
    try {
      Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
      Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);
      Imgproc.Canny(blurred, edges, 45, 140);
      Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
      Imgproc.morphologyEx(edges, edges, Imgproc.MORPH_CLOSE, kernel);
      kernel.release();
      Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
      double imageArea = source.cols() * (double) source.rows();
      List<Rect> candidates = new ArrayList<>();
      for (MatOfPoint contour : contours) {
        Rect rect = Imgproc.boundingRect(contour);
        double area = rect.area(), ratio = rect.width / (double) Math.max(1, rect.height);
        if (area >= imageArea * .055 && area <= imageArea * .98 && ratio >= .55 && ratio <= 1.35) candidates.add(rect);
      }
      candidates.sort(Comparator.comparingDouble(Rect::area).reversed());
      List<Rect> selected = new ArrayList<>();
      for (Rect candidate : candidates) if (selected.stream().noneMatch(existing -> overlap(candidate, existing) > .55)) selected.add(padded(candidate, source.cols(), source.rows()));
      if (selected.size() < 2 && source.cols() / (double) source.rows() > 1.35) selected = gridFallback(source.cols(), source.rows());
      if (selected.isEmpty()) selected.add(new Rect(0, 0, source.cols(), source.rows()));
      selected.sort(Comparator.comparingInt((Rect r) -> r.y).thenComparingInt(r -> r.x));
      return selected;
    } finally {
      contours.forEach(Mat::release); gray.release(); blurred.release(); edges.release(); hierarchy.release();
    }
  }

  private TicketPhotoRecognitionRow recognizeTicket(Mat crop, int index, int total) throws Exception {
    BufferedImage original = buffered(crop);
    String barcode = decodeBarcode(original);
    Mat prepared = preprocess(crop);
    try {
      BufferedImage clean = buffered(prepared);
      String text = normalize(ocr(clean, ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK, null));
      if (barcode == null) barcode = first(BARCODE_TEXT, text);
      String recognizedBarcode = barcode;
      String physical = all(PHYSICAL_NUMBER, text).stream().filter(v -> !v.equals(recognizedBarcode)).findFirst().orElse(null);
      Integer duration = integer(DURATION, text);
      List<LocalDate> dates = dates(text);

      BufferedImage topRight = bufferedRegion(prepared, .52, .08, .46, .48);
      String ticketText = ocr(topRight, ITessAPI.TessPageSegMode.PSM_SINGLE_WORD, "0123456789").replaceAll("[^0-9]", "");
      Integer ticketNumber = SMALL_NUMBER.matcher(ticketText).matches() ? Integer.valueOf(ticketText) : null;
      if (ticketNumber != null && ticketNumber.equals(duration)) ticketNumber = null;

      BufferedImage top = bufferedRegion(prepared, .35, 0, .65, .30);
      String topDigits = ocr(top, ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT, "0123456789").replaceAll("[^0-9\\n ]", " ");
      if (physical == null) physical = first(PHYSICAL_NUMBER, topDigits);

      if (ticketNumber == null && total > 1) {
        List<String> small = all(Pattern.compile("(?m)^\\s*([1-9]\\d{0,2})\\s*$"), text);
        ticketNumber = small.stream().map(Integer::valueOf).filter(v -> !v.equals(duration)).findFirst().orElse(null);
      }
      return new TicketPhotoRecognitionRow(barcode, physical, duration, ticketNumber,
          dates.isEmpty() ? null : dates.get(0), dates.size() < 2 ? null : dates.get(1), confidence(original, barcode, text));
    } finally { prepared.release(); }
  }

  private Mat preprocess(Mat source) {
    Mat gray = new Mat(), scaled = new Mat(), normalized = new Mat(), binary = new Mat();
    Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
    double scale = Math.max(1.5, 1500d / Math.max(1, gray.cols()));
    Imgproc.resize(gray, scaled, new Size(), scale, scale, Imgproc.INTER_CUBIC);
    Imgproc.createCLAHE(2.0, new Size(8, 8)).apply(scaled, normalized);
    Imgproc.adaptiveThreshold(normalized, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 31, 12);
    gray.release(); scaled.release(); normalized.release();
    return binary;
  }

  private String decodeBarcode(BufferedImage image) {
    Map<DecodeHintType,Object> hints = new EnumMap<>(DecodeHintType.class);
    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    for (int rotation = 0; rotation < 2; rotation++) {
      try {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Result result = new MultiFormatReader().decode(bitmap, hints);
        if (result != null && result.getText() != null && !result.getText().isBlank()) return result.getText().trim();
      } catch (NotFoundException ignored) {}
      image = rotate180(image);
    }
    return null;
  }

  private String ocr(BufferedImage image, int pageMode, String whitelist) throws Exception {
    Tesseract engine = new Tesseract();
    engine.setDatapath(tessdataPath());
    engine.setLanguage("eng");
    engine.setPageSegMode(pageMode);
    engine.setVariable("preserve_interword_spaces", "1");
    if (whitelist != null) engine.setVariable("tessedit_char_whitelist", whitelist);
    return engine.doOCR(image);
  }

  private String tessdataPath() {
    if (!configuredTessdataPath.isBlank()) return configuredTessdataPath;
    String environment = System.getenv("TESSDATA_PREFIX");
    if (environment != null && !environment.isBlank()) return environment;
    File extracted = LoadLibs.extractTessResources("tessdata");
    return extracted.getAbsolutePath();
  }

  private static BufferedImage buffered(Mat mat) throws Exception {
    MatOfByte bytes = new MatOfByte();
    try { Imgcodecs.imencode(".png", mat, bytes); return ImageIO.read(new ByteArrayInputStream(bytes.toArray())); }
    finally { bytes.release(); }
  }

  private static BufferedImage bufferedRegion(Mat mat, double x, double y, double width, double height) throws Exception {
    Rect region = new Rect((int)(mat.cols()*x), (int)(mat.rows()*y), Math.max(1,(int)(mat.cols()*width)), Math.max(1,(int)(mat.rows()*height)));
    Mat crop = new Mat(mat, region);
    try { return buffered(crop); } finally { crop.release(); }
  }

  private static BufferedImage rotate180(BufferedImage source) {
    BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
    var graphics = out.createGraphics();
    try { graphics.rotate(Math.PI, source.getWidth()/2d, source.getHeight()/2d); graphics.drawImage(source, 0, 0, null); }
    finally { graphics.dispose(); }
    return out;
  }

  private static List<Rect> gridFallback(int width, int height) {
    int columns = 3, rows = 2; List<Rect> result = new ArrayList<>();
    for (int row=0;row<rows;row++) for (int col=0;col<columns;col++) result.add(new Rect(col*width/columns,row*height/rows,width/columns,height/rows));
    return result;
  }

  private static Rect padded(Rect value, int maxWidth, int maxHeight) {
    int padX=Math.max(2,value.width/100),padY=Math.max(2,value.height/100),x=Math.max(0,value.x-padX),y=Math.max(0,value.y-padY);
    return new Rect(x,y,Math.min(maxWidth-x,value.width+padX*2),Math.min(maxHeight-y,value.height+padY*2));
  }

  private static double overlap(Rect a, Rect b) {
    int x=Math.max(a.x,b.x),y=Math.max(a.y,b.y),right=Math.min(a.x+a.width,b.x+b.width),bottom=Math.min(a.y+a.height,b.y+b.height);
    double intersection=Math.max(0,right-x)*(double)Math.max(0,bottom-y); return intersection/Math.min(a.area(),b.area());
  }

  private static double confidence(BufferedImage image, String barcode, String text) {
    double value=0; if(barcode!=null)value+=.45; if(DURATION.matcher(text).find())value+=.2; if(DATE.matcher(text).find())value+=.2; if(image.getWidth()>250&&image.getHeight()>250)value+=.15; return Math.min(1,value);
  }
  private static boolean hasValue(TicketPhotoRecognitionRow row) { return row.barcode()!=null||row.physicalTicketNumber()!=null||row.ticketNumber()!=null||row.durationHours()!=null; }
  private static String normalize(String value) { return value.replace('|','1').replaceAll("(?<=\\d)\\s+(?=\\d)","").trim(); }
  private static String first(Pattern pattern,String value){Matcher matcher=pattern.matcher(value);return matcher.find()?matcher.group(1):null;}
  private static List<String> all(Pattern pattern,String value){List<String> result=new ArrayList<>();Matcher matcher=pattern.matcher(value);while(matcher.find())result.add(matcher.group(1));return result;}
  private static Integer integer(Pattern pattern,String value){String found=first(pattern,value);return found==null?null:Integer.valueOf(found);}
  private static List<LocalDate> dates(String value){List<LocalDate> result=new ArrayList<>();Matcher matcher=DATE.matcher(value);while(matcher.find())try{result.add(LocalDate.of(Integer.parseInt(matcher.group(1)),Integer.parseInt(matcher.group(2)),Integer.parseInt(matcher.group(3))));}catch(Exception ignored){}return result;}
  private static TicketPhotoRecognitionResponse failed(String message){return new TicketPhotoRecognitionResponse(false,"BROWSER_OCR",message,List.of());}
}
