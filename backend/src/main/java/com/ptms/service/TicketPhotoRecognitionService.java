package com.ptms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptms.dto.TicketPhotoRecognitionResponse;
import com.ptms.dto.TicketPhotoRecognitionRow;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TicketPhotoRecognitionService {
  private static final Pattern BARCODE = Pattern.compile("(?<!\\d)(\\d{7})(?!\\d)");
  private static final Pattern PHYSICAL_NUMBER = Pattern.compile("(?<!\\d)(\\d{6})(?!\\d)");
  private static final Pattern DURATION = Pattern.compile("valid\\s+for\\s+(\\d{1,2})\\s*hours?", Pattern.CASE_INSENSITIVE);
  private static final Pattern DATE = Pattern.compile("(20\\d{2})[-/.](\\d{1,2})[-/.](\\d{1,2})");
  private static final Pattern SMALL_NUMBER = Pattern.compile("^[1-9]\\d{0,2}$");
  private static final long MAX_IMAGE_BYTES = 12L * 1024 * 1024;

  private final ObjectMapper json;
  private final HttpClient http;
  private final String endpoint;
  private final String key;
  private final String modelId;
  private final String apiVersion;
  private final long pollIntervalMs;
  private final int timeoutSeconds;

  public TicketPhotoRecognitionService(
      ObjectMapper json,
      @Value("${azure.document-intelligence.endpoint:}") String endpoint,
      @Value("${azure.document-intelligence.key:}") String key,
      @Value("${azure.document-intelligence.model-id:prebuilt-layout}") String modelId,
      @Value("${azure.document-intelligence.api-version:2024-11-30}") String apiVersion,
      @Value("${azure.document-intelligence.poll-interval-ms:750}") long pollIntervalMs,
      @Value("${azure.document-intelligence.timeout-seconds:45}") int timeoutSeconds) {
    this.json = json;
    this.endpoint = stripTrailingSlash(endpoint.trim());
    this.key = key.trim();
    this.modelId = modelId.trim();
    this.apiVersion = apiVersion.trim();
    this.pollIntervalMs = Math.max(250, pollIntervalMs);
    this.timeoutSeconds = Math.max(5, timeoutSeconds);
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  public TicketPhotoRecognitionResponse recognize(MultipartFile image) {
    if (!configured()) return fallback("Azure Document Intelligence is not configured; browser OCR will be used.");
    if (image == null || image.isEmpty()) return fallback("Choose a ticket image first.");
    if (image.getSize() > MAX_IMAGE_BYTES) return fallback("The image exceeds the 12 MB recognition limit.");
    String contentType = Optional.ofNullable(image.getContentType()).orElse("application/octet-stream");
    if (!contentType.startsWith("image/") && !contentType.equals("application/pdf")) return fallback("Only ticket images or PDF files can be recognized.");
    try {
      URI uri = URI.create(endpoint + "/documentintelligence/documentModels/"
          + URLEncoder.encode(modelId, StandardCharsets.UTF_8)
          + ":analyze?api-version=" + URLEncoder.encode(apiVersion, StandardCharsets.UTF_8));
      HttpRequest request = HttpRequest.newBuilder(uri)
          .timeout(Duration.ofSeconds(timeoutSeconds))
          .header("Ocp-Apim-Subscription-Key", key)
          .header("Content-Type", contentType)
          .POST(HttpRequest.BodyPublishers.ofByteArray(image.getBytes()))
          .build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 202) return fallback("Azure recognition was unavailable (HTTP " + response.statusCode() + "); browser OCR will be used.");
      String operationLocation = response.headers().firstValue("Operation-Location").orElse("");
      if (operationLocation.isBlank()) return fallback("Azure did not return a recognition operation; browser OCR will be used.");
      JsonNode result = poll(operationLocation);
      List<TicketPhotoRecognitionRow> rows = parse(result.path("analyzeResult"));
      if (rows.isEmpty()) return fallback("Azure did not identify ticket records; browser OCR will be used.");
      return new TicketPhotoRecognitionResponse(true, true, "AZURE_DOCUMENT_INTELLIGENCE",
          "Azure Document Intelligence detected " + rows.size() + " ticket" + (rows.size() == 1 ? "" : "s") + ".", rows);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return fallback("Azure recognition was interrupted; browser OCR will be used.");
    } catch (Exception e) {
      return fallback("Azure recognition was unavailable; browser OCR will be used.");
    }
  }

  private JsonNode poll(String operationLocation) throws Exception {
    long deadline = System.nanoTime() + Duration.ofSeconds(timeoutSeconds).toNanos();
    while (System.nanoTime() < deadline) {
      HttpRequest poll = HttpRequest.newBuilder(URI.create(operationLocation)).timeout(Duration.ofSeconds(15))
          .header("Ocp-Apim-Subscription-Key", key).GET().build();
      HttpResponse<String> response = http.send(poll, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() / 100 != 2) throw new IllegalStateException("Azure polling failed");
      JsonNode body = json.readTree(response.body());
      String status = body.path("status").asText("").toLowerCase(Locale.ROOT);
      if ("succeeded".equals(status)) return body;
      if ("failed".equals(status) || "canceled".equals(status)) throw new IllegalStateException("Azure analysis failed");
      Thread.sleep(pollIntervalMs);
    }
    throw new IllegalStateException("Azure analysis timed out");
  }

  private List<TicketPhotoRecognitionRow> parse(JsonNode analyzeResult) {
    JsonNode pages = analyzeResult.path("pages");
    if (!pages.isArray() || pages.isEmpty()) return List.of();
    List<TicketPhotoRecognitionRow> result = new ArrayList<>();
    for (JsonNode page : pages) result.addAll(parsePage(page));
    return result;
  }

  private List<TicketPhotoRecognitionRow> parsePage(JsonNode page) {
    double width = Math.max(1, page.path("width").asDouble(1));
    double height = Math.max(1, page.path("height").asDouble(1));
    List<OcrLine> lines = new ArrayList<>();
    for (JsonNode line : page.path("lines")) {
      Box box = box(line.path("polygon"));
      String text = normalize(line.path("content").asText(""));
      if (!text.isBlank()) lines.add(new OcrLine(text, box, confidence(line)));
    }
    if (lines.isEmpty()) return List.of();

    List<OcrLine> anchors = lines.stream().filter(l -> l.text().toLowerCase(Locale.ROOT).contains("one galle face"))
        .sorted(Comparator.comparingDouble((OcrLine l) -> l.box().cy()).thenComparingDouble(l -> l.box().cx())).toList();
    int columns;
    int rows;
    if (anchors.size() > 1) {
      columns = clusterCount(anchors.stream().map(a -> a.box().cx()).toList(), width * .18);
      rows = clusterCount(anchors.stream().map(a -> a.box().cy()).toList(), height * .18);
    } else if (width / height > 1.25) {
      columns = 3;
      rows = 2;
    } else {
      columns = 1;
      rows = 1;
    }
    int count = Math.max(1, Math.max(anchors.size(), columns * rows));
    List<List<OcrLine>> regions = new ArrayList<>();
    for (int i = 0; i < count; i++) regions.add(new ArrayList<>());
    for (OcrLine line : lines) {
      int col = Math.min(columns - 1, Math.max(0, (int) (line.box().cx() / (width / columns))));
      int row = Math.min(rows - 1, Math.max(0, (int) (line.box().cy() / (height / rows))));
      regions.get(Math.min(count - 1, row * columns + col)).add(line);
    }

    List<TicketPhotoRecognitionRow> result = new ArrayList<>();
    for (int i = 0; i < regions.size(); i++) {
      int col = i % columns;
      int row = i / columns;
      TicketPhotoRecognitionRow ticket = parseRegion(regions.get(i), col * width / columns, row * height / rows,
          width / columns, height / rows);
      if (ticket.barcode() != null || ticket.physicalTicketNumber() != null || ticket.ticketNumber() != null) result.add(ticket);
    }
    return result;
  }

  private TicketPhotoRecognitionRow parseRegion(List<OcrLine> lines, double left, double top, double width, double height) {
    String content = String.join("\n", lines.stream().map(OcrLine::text).toList());
    String barcode = match(BARCODE, content);
    String physical = lines.stream().filter(l -> l.box().cy() < top + height * .30).map(OcrLine::text)
        .map(t -> match(PHYSICAL_NUMBER, t)).filter(v -> v != null && !v.equals(barcode)).findFirst().orElse(null);
    if (physical == null) physical = allMatches(PHYSICAL_NUMBER, content).stream().filter(v -> !v.equals(barcode)).findFirst().orElse(null);
    Integer duration = integerMatch(DURATION, content);
    List<LocalDate> dates = dates(content);
    Integer ticketNumber = lines.stream()
        .filter(l -> l.box().cx() > left + width * .55 && l.box().cy() > top + height * .12 && l.box().cy() < top + height * .58)
        .map(l -> l.text().replaceAll("[^0-9]", ""))
        .filter(v -> SMALL_NUMBER.matcher(v).matches())
        .map(Integer::valueOf).filter(v -> !v.equals(duration)).findFirst().orElse(null);
    double confidence = lines.stream().mapToDouble(OcrLine::confidence).filter(v -> v > 0).average().orElse(0);
    return new TicketPhotoRecognitionRow(barcode, physical, duration, ticketNumber,
        dates.isEmpty() ? null : dates.get(0), dates.size() < 2 ? null : dates.get(1), confidence);
  }

  private static Box box(JsonNode polygon) {
    if (!polygon.isArray() || polygon.size() < 4) return new Box(0, 0, 0, 0);
    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = 0, maxY = 0;
    for (int i = 0; i + 1 < polygon.size(); i += 2) {
      double x = polygon.get(i).asDouble(), y = polygon.get(i + 1).asDouble();
      minX = Math.min(minX, x); minY = Math.min(minY, y); maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
    }
    return new Box(minX, minY, maxX, maxY);
  }

  private static double confidence(JsonNode line) {
    JsonNode words = line.path("words");
    if (!words.isArray()) return 0;
    double total = 0; int count = 0;
    for (JsonNode word : words) if (word.has("confidence")) { total += word.path("confidence").asDouble(); count++; }
    return count == 0 ? 0 : total / count;
  }

  private static int clusterCount(List<Double> values, double gap) {
    List<Double> sorted = values.stream().sorted().toList();
    if (sorted.isEmpty()) return 1;
    int clusters = 1; double previous = sorted.get(0);
    for (double value : sorted) { if (value - previous > gap) clusters++; previous = value; }
    return Math.max(1, clusters);
  }

  private static List<LocalDate> dates(String value) {
    List<LocalDate> dates = new ArrayList<>(); Matcher matcher = DATE.matcher(value);
    while (matcher.find()) try { dates.add(LocalDate.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)))); } catch (Exception ignored) {}
    return dates;
  }

  private static Integer integerMatch(Pattern pattern, String value) {
    Matcher matcher = pattern.matcher(value); return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
  }

  private static String match(Pattern pattern, String value) {
    Matcher matcher = pattern.matcher(value); return matcher.find() ? matcher.group(1) : null;
  }

  private static List<String> allMatches(Pattern pattern, String value) {
    List<String> values = new ArrayList<>(); Matcher matcher = pattern.matcher(value);
    while (matcher.find()) values.add(matcher.group(1)); return values;
  }

  private static String normalize(String value) { return value.replace('|', '1').replaceAll("(?<=\\d)\\s+(?=\\d)", "").trim(); }
  private static String stripTrailingSlash(String value) { return value.replaceAll("/+$", ""); }
  private boolean configured() { return !endpoint.isBlank() && !key.isBlank(); }
  private TicketPhotoRecognitionResponse fallback(String message) { return new TicketPhotoRecognitionResponse(configured(), false, "BROWSER_OCR", message, List.of()); }

  private record Box(double left, double top, double right, double bottom) {
    double cx() { return (left + right) / 2; }
    double cy() { return (top + bottom) / 2; }
  }
  private record OcrLine(String text, Box box, double confidence) {}
}
