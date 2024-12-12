# Anderson POC Spring Boot Demo Project

Simple Spring Boot application that provides a basic contacts API with PostgreSQL persistence and optional Kafka event publishing.

## Prerequisites

- [asdf](https://asdf-vm.com/) for version management
- Docker and docker compose for local development
- Java 21
- Maven 3.9.6

## Setup

1. Install required asdf plugins:

```bash
asdf plugin add java
asdf plugin add maven
```

2. Install dependencies (from project root):

```bash
asdf install
```

## Development

Build the application:

```bash
mvn clean package
```

Run with Docker (includes PostgreSQL and Kafka):

```bash
docker compose up -d
```

The application will be available at http://localhost:8080
Kafka UI will be available at http://localhost:8081

## Docker Compose Details

The application uses Docker Compose for local development with several services:

### Services

- `app` - The Spring Boot application
- `postgres` - PostgreSQL database
- `zookeeper` - Required for Kafka
- `kafka` - Kafka broker
- `kafka-init` - Creates required Kafka topics
- `kafka-ui` - Web UI for Kafka monitoring

### Startup Order

Services start in the following order:

1. PostgreSQL (with health check)
2. Zookeeper
3. Kafka
4. Kafka initialization
5. Application (waits for PostgreSQL and Kafka)

### Persistence

- PostgreSQL data is persisted in a named volume: `postgres_data`
- To completely reset the database: `docker compose down -v`

### Local Development

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f app

# Restart just the application
docker compose restart app

# Stop everything and remove volumes
docker compose down -v
```

## Testing

Run the tests with:

```bash
mvn test
```

Tests use an H2 in-memory database and don't require PostgreSQL or Kafka.

## API Endpoints

### Health Check

- `GET /` - Returns "OK"

### Contacts API

All contacts endpoints require the `X-Goog-Authenticated-User-Id` header. The value of this header is used as the user identifier for storing and retrieving contacts.

- `POST /contact` - Create a contact
- `GET /contact` - Get all contacts for the authenticated user
- `PUT /contact/{contactId}` - Update a specific contact
- `DELETE /contact/{contactId}` - Delete a specific contact

Example contact creation:

```bash
curl -X POST http://localhost:8080/contact \
-H "Content-Type: application/json" \
-H "X-Goog-Authenticated-User-Id: user123" \
-d '{
  "name": "John Smith",
  "email": "john@example.com",
  "phone": "555-1234"
}'
```

Example contact update:

```bash
curl -X PUT http://localhost:8080/contact/contact-id-here \
-H "Content-Type: application/json" \
-H "X-Goog-Authenticated-User-Id: user123" \
-d '{
  "name": "John Smith Updated",
  "email": "john.updated@example.com"
}'
```

## Database Configuration

The application uses PostgreSQL for persistence:

- Database migrations are managed by Flyway
- Migrations are automatically applied on startup
- Default connection: localhost:5432/contacts
- Default credentials: postgres/postgres

To check the database:

```bash
# Connect to PostgreSQL
docker compose exec postgres psql -U postgres -d contacts

# List tables
\dt

# View contacts
SELECT * FROM contacts;
```

Environment variables for database configuration:

- `POSTGRES_URL` - Database URL (default: jdbc:postgresql://localhost:5432/contacts)
- `POSTGRES_USER` - Database username (default: postgres)
- `POSTGRES_PASSWORD` - Database password (default: postgres)

## Kafka Integration

The application can optionally publish contact events (create/update/delete) to Kafka.

### Running with Kafka

1. Start the environment with docker compose:

```bash
docker compose up -d
```

2. View messages in Kafka UI:
   - Open http://localhost:8081
   - Navigate to Topics > contact-events
   - View messages in the Messages tab
   - Check consumer logs: `docker compose logs -f kafka-consumer`

### Configuration

Kafka integration can be enabled/disabled via environment variables:

- `KAFKA_ENABLED` - Enable/disable Kafka integration (default: false)
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka broker addresses
- `KAFKA_TOPIC` - Topic for contact events

## Project Structure

- `/src/main/java/com/anderson/demo` - Main application code
- `/src/main/resources/db/migration` - Flyway database migrations
- `/src/test/java/com/anderson/demo` - Test code
- `Dockerfile` - Docker configuration
- `docker-compose.yml` - Docker Compose configuration for local development
- `pom.xml` - Maven dependencies and build configuration
