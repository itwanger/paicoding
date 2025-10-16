# AGENTS.md

This file provides guidance to Qoder (qoder.com) when working with code in this repository.

## Project Overview

PaiCoding is a Spring Boot-based community platform for technical content sharing. Multi-module Maven project with layered architecture separating API definitions, business logic, core utilities, frontend resources, and web endpoints.

## Build and Test Commands

### Build
```bash
# Clean and install all modules
mvn clean install -DskipTests=true

# Build for specific environment (dev/test/pre/prod)
mvn clean install -DskipTests=true -P<env>

# Build web module only
cd paicoding-web && mvn clean package spring-boot:repackage -DskipTests=true -Pprod
```

### Test
```bash
# Run all tests
mvn test

# Run tests in specific module
cd paicoding-web && mvn test

# Run single test class
mvn test -Dtest=ClassName

# Run single test method
mvn test -Dtest=ClassName#methodName
```

### Run Application
```bash
# Local development (requires MySQL and Redis running)
# Entry point: QuickForumApplication in paicoding-web module
# Default port: 8080
# Configure database in: paicoding-web/src/main/resources-env/dev/application-dal.yml

# Production deployment
./launch.sh start        # Build and deploy
./launch.sh restart      # Restart existing deployment
```

## Architecture

### Module Dependencies
```
paicoding-web
├── depends on: paicoding-ui, paicoding-service
│
paicoding-service  
├── depends on: paicoding-core, paicoding-api
│
paicoding-core
├── depends on: paicoding-api
│
paicoding-api
└── (base module: entities, DTOs, VOs, enums)
```

### Key Modules
- **paicoding-api**: Entity definitions, DTOs, VOs, common enums
- **paicoding-core**: Utilities, search, cache, recommendations, common components
- **paicoding-service**: Business logic, MyBatis-Plus database operations
- **paicoding-ui**: Thymeleaf templates, JavaScript, CSS, static resources
- **paicoding-web**: Controllers, REST endpoints, `QuickForumApplication` entry point, global exception handling, authentication

### Configuration Structure
- Environment configs in `paicoding-web/src/main/resources-env/<env>/`
  - `application-dal.yml`: Database config
  - `application-image.yml`: Image upload config
  - `application-web.yml`: Web config
- Main configs in `paicoding-web/src/main/resources/`
  - `application.yml`: Main entry
  - `application-config.yml`: Site configuration
  - `logback-spring.xml`: Logging

### Technology Stack
- Spring Boot 2.7.1, Java 8+
- MyBatis-Plus for ORM
- Thymeleaf for SSR
- Redis for caching/sessions
- ElasticSearch for search
- RabbitMQ for messaging
- MongoDB for NoSQL
- Liquibase for schema migrations (in `paicoding-web/src/main/resources/liquibase`)

## Development Guidelines

### Database Changes
- Add Liquibase changesets to `paicoding-web/src/main/resources/liquibase`
- Database auto-creates on first startup (default: `paicoding`)
- Use MyBatis-Plus for database operations
- Entity classes in paicoding-api module

### Adding New Features
- Follow layered architecture: API → Service → Core
- Check existing code patterns in neighboring files
- Use libraries already present in the project (check root `pom.xml`)
- Frontend changes go in paicoding-ui module

### API Documentation
- Swagger UI available at `/doc.html`
- Uses Knife4j (knife4j-openapi2-spring-boot-starter)

### Testing
- Supports JUnit and Spock (Groovy-based BDD)
- Test files in `paicoding-web/src/test/java` and `paicoding-web/src/test/groovy`
