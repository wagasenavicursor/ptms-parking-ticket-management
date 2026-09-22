# PTMS Enterprise

Parking Ticket Management System rebuilt using Java 21, Spring Boot, MySQL, Angular, Maven, Git, and WildFly-compatible WAR packaging.

## Architecture
Backend uses layered architecture: `controller -> service -> repository -> domain`, with DTOs at API boundaries. Scanner parsing is implemented in Angular as a reusable service. Business rules are isolated in services.

## Run backend
```bash
cd backend
mvn spring-boot:run
```

## Run frontend
```bash
cd frontend
npm install
npm start
```

## MySQL
Default connection: `jdbc:mysql://localhost:3306/ptms`, user/password `ptms/ptms`. Override with `SPRING_DATASOURCE_*` environment variables.

## Tests
```bash
cd backend && mvn test
cd frontend && npm test -- --watch=false
```

## Build WAR
```bash
./scripts/build.sh
```
The deployable file is `backend/target/ptms.war`.

## Barcode scanner
Use a USB HID keyboard scanner with no prefix and Enter/Carriage Return suffix. Scanner support is available in inventory, issue-ticket workflow, and reconciliation.

## Local ticket-photo recognition
Bulk ticket photos are processed entirely on the PTMS server; images are not sent to an external AI service. The recognition pipeline uses OpenCV to detect and crop each physical ticket, ZXing to read printed barcodes, and Tess4J/Tesseract to read printed and handwritten text. If server recognition cannot complete, the existing in-browser OCR is used as a fallback.

Tess4J includes English recognition data by default. To use a separately installed or tuned Tesseract data folder, set:

```bash
export PTMS_TESSDATA_PATH="/path/to/tessdata"
```

On Windows, install the current Microsoft Visual C++ Redistributable required by Tess4J's native libraries. Recognition remains assistive: guards must review and may correct every detected barcode, printed physical number, circled ticket number, duration, issue date, and valid-till date before saving.

## Git workflow
- `main`: stable
- `developer`: integration branch
- `feature/*`: feature work
