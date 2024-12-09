# Anderson POC Spring Boot Demo Project

Simple Spring Boot application that provides a basic contacts API with optional Kafka event publishing.

## Prerequisites

- [asdf](https://asdf-vm.com/) for version management
- Docker and docker-compose for local development and Kafka integration
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

Build and run the application without Kafka:

```bash
mvn clean package
mvn spring-boot:run
```

Run with Docker and Kafka:

```bash
docker-compose up -d
```

The application will be available at http://localhost:8080
Kafka UI will be available at http://localhost:8081

## Testing

Run the tests with:

```bash
mvn test
```

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
  "phone": "555-1234",
  "twitter": "@johnsmith",
  "department": "Engineering"
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

## Kafka Integration

The application can optionally publish contact events (create/update/delete) to Kafka.

### Running with Kafka

1. Start the environment with docker-compose:

```bash
docker-compose up -d
```

2. View messages in Kafka UI:
   - Open http://localhost:8081
   - Navigate to Topics > contact-events
   - View messages in the Messages tab
   - Check consumer logs: `docker-compose logs -f kafka-consumer`

### Configuration

Kafka integration can be enabled/disabled via environment variables:

- `KAFKA_ENABLED` - Enable/disable Kafka integration (default: false)
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka broker addresses
- `KAFKA_TOPIC` - Topic for contact events

Note: The contacts API accepts arbitrary JSON fields, making it flexible for different use cases. Currently, contacts are stored in a local JSON file (contacts.json) - this is a temporary solution for the POC phase and will be replaced with proper persistence in a future iteration. The file is ephemeral and resets when the Docker container restarts.

## Project Structure

- `/src/main/java/com/anderson/demo` - Main application code
- `/src/test/java/com/anderson/demo` - Test code
- `Dockerfile` - Docker configuration
- `docker-compose.yml` - Docker Compose configuration for local development with Kafka
- `pom.xml` - Maven dependencies and build configuration
- `contacts.json` - Temporary local storage for contacts (to be replaced with proper persistence)
