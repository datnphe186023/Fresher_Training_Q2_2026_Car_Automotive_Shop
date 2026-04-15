# Car Enhancement Shop Management System - Backend

Spring Boot 3.x backend API for the Car Enhancement Shop Management System.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+

## Project Structure

```
src/main/java/com/carshop/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Request DTOs
│   └── response/    # Response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions
├── mapper/          # Entity-DTO mappers
├── repository/      # Spring Data repositories
├── security/        # Security components
├── service/         # Business logic
└── util/            # Utility classes
```

## Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE carshop;
```

2. Update database credentials in `src/main/resources/application.properties`

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080

## Building the Project

```bash
mvn clean install
```

## Configuration

Key configuration properties in `application.properties`:
- Database connection settings
- JWT secret and token expiry
- Server port
- Logging levels
