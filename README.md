# Anderson POC Spring Boot Demo Project

Simple Spring Boot application that responds with "OK" at the root endpoint. More to be added.

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

## Project Structure

- `/src/main/java/com/anderson/demo` - Main application code
- `/src/test/java/com/anderson/demo` - Test code
- `Dockerfile` - Docker configuration
- `pom.xml` - Maven dependencies and build configuration

## Endpoints

- `GET /` - Returns "OK"
