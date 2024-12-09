# Anderson POC Spring Boot Demo Project

Simple Spring Boot application that provides a basic contacts API.

## Prerequisites

- [asdf](https://asdf-vm.com/) for version management
- Docker (optional, for container deployment)

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

Build and run the application:

```bash
mvn clean package
mvn spring-boot:run
```

The application will be available at http://localhost:8080

## Testing

Run the tests with:

```bash
mvn test
```

## Docker Deployment

Build and run with Docker:

```bash
docker build -t spring-demo .
docker run -p 8080:8080 spring-demo
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

Note: The contacts API accepts arbitrary JSON fields, making it flexible for different use cases. Currently, contacts are stored in a local JSON file (contacts.json) - this is a temporary solution for the POC phase and will be replaced with proper persistence in a future iteration. The file is ephemeral and resets when the Docker container restarts.

## Project Structure

- `/src/main/java/com/anderson/demo` - Main application code
- `/src/test/java/com/anderson/demo` - Test code
- `Dockerfile` - Docker configuration
- `pom.xml` - Maven dependencies and build configuration
- `contacts.json` - Temporary local storage for contacts (to be replaced with proper persistence)
