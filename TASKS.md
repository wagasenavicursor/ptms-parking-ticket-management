# PTMS Enterprise Rebuild — Development Tasks

## Goal
Rebuild the Parking Ticket Management System as a maintainable full-stack application using:
- Java 21 + Spring Boot
- MySQL 8
- Angular
- Maven
- Git with a `developer` branch
- WildFly-compatible WAR deployment
- Unit and integration tests
- Layered backend architecture and clean-code practices

## Phase 1 — Project Foundation
- [x] T-001 Create the implementation task breakdown.
- [x] T-002 Create Git repository and `developer` branch.
- [x] T-003 Create backend/frontend/project folder structure.
- [x] T-004 Add `.gitignore`, README, environment configuration, Docker Compose, and CI workflow.
- [x] T-005 Configure Maven Spring Boot WAR build for deployment to WildFly.
- [x] T-006 Configure MySQL persistence and Flyway database migrations.

## Phase 2 — Backend Domain Model
- [x] T-010 Implement Employee domain model.
- [x] T-011 Implement Visitor domain model with auto-generated visitor IDs.
- [x] T-012 Implement Parking Ticket inventory domain model.
- [x] T-013 Implement Ticket Issue / Ticket Issue Item domain model.
- [x] T-014 Implement Reconciliation domain model.
- [x] T-015 Add enums for person type, ticket status, and issue status.

## Phase 3 — Backend Architecture
- [x] T-020 Create repository layer using Spring Data JPA.
- [x] T-021 Create DTO/request/response layer.
- [x] T-022 Create mapper layer.
- [x] T-023 Create service layer with transactional boundaries.
- [x] T-024 Add REST controller layer.
- [x] T-025 Add centralized exception handling and validation.
- [x] T-026 Add CORS and API configuration.

## Phase 4 — Business Rules
- [x] T-030 Implement employee parking-pass coverage:
  - Monday-Friday: 06:00-12:00
  - Saturday/Sunday: 06:00-12:30
  - only uncovered parking time requires tickets.
- [x] T-031 Implement visitor rule: visitors require tickets for the full parking period.
- [x] T-032 Implement ticket combination calculation.
- [x] T-033 Validate ticket availability and ticket-hour combination before issuing.
- [x] T-034 Implement pending -> completed ticket issue workflow.
- [x] T-035 Allow pending requests to be edited/deleted only before completion.
- [x] T-036 Update ticket inventory atomically when an issue is completed.

## Phase 5 — Physical Barcode Scanner Support
- [x] T-040 Implement bulk ticket-inventory scanning API.
- [x] T-041 Implement scanner-friendly inventory UI with continuous scanning.
- [x] T-042 Implement automatic barcode capture for ticket issuing.
- [x] T-043 Validate scanned barcode against required duration and availability.
- [x] T-044 Implement automatic barcode capture on reconciliation.
- [x] T-045 Add scanner support to single-ticket entry.
- [x] T-046 Prevent duplicate barcode scanning.
- [x] T-047 Implement reusable Angular BarcodeScannerService for USB HID keyboard scanners.
- [x] T-048 Support scanners configured with Enter/Carriage Return suffix.

## Phase 6 — REST APIs
- [x] T-050 Employees CRUD API.
- [x] T-051 Visitors CRUD API.
- [x] T-052 Ticket inventory CRUD and scanner API.
- [x] T-053 Issue preview / ticket combination API.
- [x] T-054 Create, update, delete and complete issue APIs.
- [x] T-055 Issued ticket register API.
- [x] T-056 Remaining inventory API.
- [x] T-057 Reconciliation validation API.
- [x] T-058 Dashboard summary API.

## Phase 7 — Angular UI
- [x] T-060 Create responsive application shell and navigation.
- [x] T-061 Dashboard with summary cards and ticket-type inventory chart.
- [x] T-062 Employee management page.
- [x] T-063 Visitor management page.
- [x] T-064 Ticket inventory page with bulk scanner mode.
- [x] T-065 Issue Tickets workflow with Employee/Visitor selection and name search.
- [x] T-066 Physical barcode validation and scanner feedback.
- [x] T-067 FIFO issue request queue.
- [x] T-068 Issued Register / issue-history view.
- [x] T-069 Reconciliation page with continuous scanner mode.
- [x] T-070 Responsive desktop/mobile layout.

## Phase 8 — Testing & Quality
- [x] T-080 Unit test ticket-combination algorithm.
- [x] T-081 Unit test parking-pass coverage calculation.
- [x] T-082 Backend integration test using H2 test database and MockMvc.
- [x] T-083 Angular unit tests for barcode scanner behavior.
- [x] T-084 Add Maven test configuration.
- [x] T-085 Add GitHub Actions CI workflow.

## Phase 9 — Deployment
- [x] T-090 Package backend as `ptms.war`.
- [x] T-091 Add WildFly Docker deployment definition.
- [x] T-092 Add MySQL Docker configuration.
- [x] T-093 Add build/deploy scripts.
- [x] T-094 Document local development and WildFly deployment.

## Phase 10 — Git / GitHub
- [x] T-100 Initialize local Git repository.
- [x] T-101 Create and use `developer` branch.
- [x] T-102 Add initial project commit.
- [x] T-103 Add GitHub Actions workflow.
- [ ] T-104 Publish repository to GitHub and push `developer` branch.
  - Requires authorization to the user's GitHub account/connector.
