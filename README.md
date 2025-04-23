# Terms and conditions

This application provides an API for terms and conditions text (with versioning data) and a service for users to accept
the terms and conditions on behalf of organizations. 

For a broader understanding of the system’s context, refer to
the [architecture documentation](https://github.com/Informasjonsforvaltning/architecture-documentation) wiki. For more
specific context on this application, see the **Registration** and **IAM** subsystem section.

## Getting Started

These instructions will give you a copy of the project up and running on your local machine for development and testing
purposes.

### Prerequisites

Ensure you have the following installed:

- Java 21
- Maven
- Docker

### Running locally

Clone the repository

```sh
git clone https://github.com/Informasjonsforvaltning/terms-and-conditions.git
cd terms-and-conditions
```

Start MongoDB and the application (either through your IDE using the dev profile, or via CLI):

```sh
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```

### API Documentation (OpenAPI)

Once the application is running locally, the API documentation can be accessed
at http://localhost:8080/swagger-ui/index.html

### Running tests

```sh
mvn verify
```
