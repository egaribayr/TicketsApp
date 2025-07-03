# AWS running instace

- [Swagger UI (API Docs & Test)](http://ec2-3-143-220-90.us-east-2.compute.amazonaws.com:8080/swagger-ui/index.html)
- [Grafana Monitoring](http://ec2-3-143-220-90.us-east-2.compute.amazonaws.com:3000/)


# TicketsApp

TicketsApp is a Spring Boot-based ticket management system designed to help teams track, update, and manage support or issue tickets efficiently. The application supports ticket creation, updates, history tracking, and bulk import via CSV files.

## Features

- Create, update, and view tickets
- Track ticket history and changes (status, assignment, comments, etc.)
- Bulk import tickets from CSV files
- RESTful API endpoints for integration
- JPA-based persistence
- Logging for all service and controller actions
- API documentation and testing via Swagger UI
- Monitoring with Grafana (via Docker Compose)
- Centralized log aggregation with Loki (logs from the application are fed to Grafana via Loki)
- Code formatting enforced with Spotless

## API Endpoints

| Method | Endpoint                        | Description                        |
|--------|----------------------------------|------------------------------------|
| POST   | `/api/tickets`                  | Create a new ticket                |
| PUT    | `/api/tickets/{id}`             | Update an existing ticket (status, subject, description, assignment, comments; every update is recorded in the ticket's history) |
| GET    | `/api/tickets`                  | Retrieves a list of tickets (optionally filter by the assigned to user id)    |
| GET    | `/api/tickets/{id}/history`     | Get ticket history (optionally filter by change type) |
| POST   | `/api/tickets/bulkimport`       | Bulk import tickets from CSV file  |

**Note:**
- The `PUT /api/tickets/{id}` endpoint can be used to update the status, subject, description, assignment, and add comments to a ticket. Every update (of any field) is automatically recorded in the ticket's history for full traceability.

## Getting Started

### Prerequisites
- Java 17+
- Gradle
- Docker (optional, for database, app, and monitoring)

### Running the App

1. **Clone the repository:**
   ```sh
   git clone <your-repo-url>
   cd demo
   ```
2. **Start everything:**
   ```sh
   docker-compose up -d
   ```
   The provided `docker-compose.yml` will start:
   - The database
   - The TicketsApp application
   - Grafana for monitoring
   - Loki for log aggregation (application logs are sent to Grafana via Loki)

3. **Access the API documentation and test endpoints:**
   - Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser to use the Swagger UI for interactive API testing and documentation.

4. **Access Grafana for monitoring and logs:**
   - Open [http://localhost:3000](http://localhost:3000) (default credentials: admin/admin)
   - View application logs in Grafana, powered by Loki

### Running Tests

```sh
./gradlew test
```

## Project Structure

- `src/main/java/com/tickets/controller` - REST controllers
- `src/main/java/com/tickets/service` - Business logic
- `src/main/java/com/tickets/model` - JPA entities
- `src/main/java/com/tickets/dto` - Data transfer objects
- `src/main/java/com/tickets/repository` - Spring Data repositories
- `src/main/java/com/tickets/util` - Utility classes and mappers
- `src/test/java/com/tickets` - Unit tests

## CSV Import Format

The CSV file for bulk import should have the following columns:

```
subject,description,status
Example subject,Example description,NEW
```

## Logging

All service and controller actions are logged using SLF4J for easier debugging and monitoring. Logs are aggregated and visualized in Grafana using Loki.

## License

This project is licensed under the MIT License.

## TODO

- Secure API
- Keep track of which user updated a ticket
- Add CI/CD

## Default Users

| id                                   | role         | username      |
|---------------------------------------|--------------|--------------|
| e6e25d84-ffae-400c-8223-d5cc4201a1dd | ROLE_USER    | user1        |
| d0740021-c9b6-44d3-86bc-e44e0e08a0ce | ROLE_USER    | user2        |
| df97e1d4-a3f0-44f7-80db-6265e91e1c7d | ROLE_SUPPORT | supportUser1 |
| c4633018-d532-44c6-b043-a610a4054c97 | ROLE_SUPPORT | supportUser2 |
| bb5cf2fd-4dd7-4893-817c-510ff0798dd3 | ROLE_PRODUCT | productUser  |
