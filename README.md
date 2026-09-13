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

## Git workflow
- `main`: stable
- `developer`: integration branch
- `feature/*`: feature work
