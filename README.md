# JPAWithTestContainers

A Java project demonstrating JPA (Jakarta Persistence API) testing with Testcontainers, using PostgreSQL as the database.

## Overview

This project showcases how to:
- Set up JPA with PostgreSQL using Testcontainers for testing
- Implement proper test isolation
- Handle database schema generation
- Manage test data effectively
- Use modern Java testing practices

## Prerequisites

- Java 21 or later
- Maven 3.8 or later
- Docker Desktop (for running Testcontainers)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── jpawithtestcontainers/
│   │           └── model/
│   │               └── Customer.java
│   └── resources/
│       └── META-INF/
│           └── persistence.xml
└── test/
    ├── java/
    │   └── com/
    │       └── jpawithtestcontainers/
    │           └── TestContainerJPA.java
    └── resources/
        └── META-INF/
            └── persistence.xml
```

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/aripddev/JPAWithTestContainers.git
   cd JPAWithTestContainers
   ```

2. Make sure Docker Desktop is running

3. Build the project:
   ```bash
   mvn clean install
   ```

## Running Tests

Run all tests:
```bash
mvn test
```

## Key Features

### Test Isolation
- Each test runs in isolation with a clean database state
- Proper setup and teardown of test data
- Consistent test environment using Testcontainers

### Database Management
- PostgreSQL container managed by Testcontainers
- Automatic schema generation
- Proper transaction handling

### Testing Practices
- Comprehensive assertions
- Clear test naming and organization
- Proper resource cleanup

## Dependencies

- Jakarta Persistence API 3.0
- EclipseLink 5.0.0-B01
- PostgreSQL JDBC Driver 42.7.5
- Testcontainers 1.20.6
- JUnit Jupiter 5.13.0-M1

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
