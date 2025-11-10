# Project Overview

This is a Java-based community system built with a multi-module Maven project structure. The backend is powered by the Spring Boot framework, and it utilizes a variety of technologies including MyBatis-Plus for data persistence, Redis for caching, and ElasticSearch for search functionalities. The project is organized into several modules: `paicoding-api`, `paicoding-core`, `paicoding-service`, `paicoding-ui`, and `paicoding-web`. The `paicoding-web` module serves as the main entry point for the application.

# Building and Running

## Building the Project

To build the project, you can use Maven. The `pom.xml` file in the root directory defines different profiles for various environments. To build the project for a specific environment, you can use the `-P` flag with the `mvn` command.

For example, to build the project for the production environment, you can run the following command:

```bash
mvn clean install -DskipTests=true -Pprod
```

## Running the Application

The main entry point for the application is the `com.github.paicoding.forum.web.QuickForumApplication` class in the `paicoding-web` module. You can run this class from your IDE to start the application.

Alternatively, you can run the application from the command line using the following command:

```bash
java -jar paicoding-web/target/paicoding-web-0.0.1-SNAPSHOT.jar
```

# Development Conventions

## Code Style

The project uses the standard Java code style. The use of Lombok suggests a preference for reducing boilerplate code.

## Testing

The project includes tests written using JUnit and Spock. The tests are located in the `src/test` directory of each module.

## Configuration

The project's configuration is managed through YAML files located in the `paicoding-web/src/main/resources` directory. The main configuration file is `application.yml`, which imports other configuration files for different aspects of the application.
