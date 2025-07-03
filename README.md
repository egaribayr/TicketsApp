# TicketsApp

TicketsApp is a Spring Boot-based ticket management system designed to help teams track, update, and manage support or issue tickets efficiently. The application supports ticket creation, updates, history tracking, and bulk import via CSV files.

## Features

- Create, update, and view tickets
- Track ticket history and changes (status, assignment, comments, etc.)
- Bulk import tickets from CSV files
- RESTful API endpoints for integration
- JPA-based persistence
- Logging for all service and controller actions

## API Endpoints

| Method | Endpoint                        | Description                        |
|--------|----------------------------------|------------------------------------|
| POST   | `/api/tickets`                  | Create a new ticket                |
| PUT    | `/api/tickets/{id}`             | Update an existing ticket          |
| GET    | `/api/tickets/{id}/history`     | Get ticket history (optionally filter by change type) |
| POST   | `/api/tickets/bulkimport`       | Bulk import tickets from CSV file  |

## Getting Started

### Prerequisites
- Java 17+
- Gradle
- Docker (optional, for database)

### Running the App

1. **Clone the repository:**
   ```sh
   git clone <your-repo-url>
   cd demo
   ```
2. **Start the database (optional):**
   ```sh
   docker-compose up -d
   ```
3. **Build and run the app:**
   ```sh
   ./gradlew bootRun
   ```

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

All service and controller actions are logged using SLF4J for easier debugging and monitoring.

## License

This project is licensed under the MIT License.
