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

## Azure ticket-photo recognition
Bulk ticket photos use Azure AI Document Intelligence first and automatically fall back to in-browser OCR when Azure is not configured or unavailable. Create an Azure AI Document Intelligence resource, then set these backend environment variables before starting PTMS:

```bash
export AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT="https://YOUR-RESOURCE.cognitiveservices.azure.com"
export AZURE_DOCUMENT_INTELLIGENCE_KEY="YOUR-RESOURCE-KEY"
mvn -f backend/pom.xml spring-boot:run
```

The default model is `prebuilt-layout` with API version `2024-11-30`. Optional overrides are `AZURE_DOCUMENT_INTELLIGENCE_MODEL_ID`, `AZURE_DOCUMENT_INTELLIGENCE_API_VERSION`, `AZURE_DOCUMENT_INTELLIGENCE_POLL_INTERVAL_MS`, and `AZURE_DOCUMENT_INTELLIGENCE_TIMEOUT_SECONDS`. Keep the key only in the backend environment; never put it in Angular configuration or commit it to Git.

Recognition is assistive: the inventory screen creates one editable row per detected physical ticket, and the user must compare barcode, printed physical number, circled number, duration, issue date, and valid-till date with the hard copy before saving.

## Git workflow
- `main`: stable
- `developer`: integration branch
- `feature/*`: feature work
