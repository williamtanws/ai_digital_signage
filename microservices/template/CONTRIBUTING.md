# Contributing to SAT Microservices Template

Thank you for your interest in contributing! This document provides guidelines and best practices for contributing to this project.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Architecture Principles](#architecture-principles)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Commit Message Guidelines](#commit-message-guidelines)

## Code of Conduct

This project adheres to professional standards. We expect all contributors to:
- Be respectful and inclusive
- Focus on constructive feedback
- Prioritize project goals and quality
- Maintain confidentiality of business information

## Getting Started

### Prerequisites
1. Java 17 or higher
2. Maven 3.8+
3. Docker and Docker Compose
4. IDE (IntelliJ IDEA or VS Code recommended)
5. Git

### Setup
```bash
# Clone the repository
git clone <repository-url>
cd sat_microservices_guide_template

# Build the project
mvn clean install

# Run tests
mvn test

# Start dependencies
docker-compose up -d kafka redis

# Run the application
mvn spring-boot:run
```

## Architecture Principles

### MUST Follow
1. **Dependency Rule**: Dependencies always point inward (Infrastructure → Application → Domain)
2. **Domain Purity**: Domain layer has NO framework dependencies
3. **CQRS**: Separate command and query operations
4. **Port-Adapter Pattern**: Use interfaces for external integrations
5. **Rich Domain Model**: Business logic belongs in domain entities, not services

### Project Structure
```
src/main/java/com/allianz/sat/
├── domain/           # Pure business logic (no Spring, no framework)
├── application/      # Use cases and orchestration
└── infrastructure/   # Technical implementations
```

## Development Workflow

### Adding New Features

#### 1. Domain First
```java
// Start with domain model
public class NewAggregate implements AggregateRoot<NewAggregateId> {
    // Business logic here
}
```

#### 2. Define Use Cases
```java
// Application layer - input port
public interface CreateNewAggregateUseCase {
    NewAggregate create(CreateCommand command);
}
```

#### 3. Implement Application Service
```java
// Application layer - service implementing use case
@Service
public class NewAggregateCommandService implements CreateNewAggregateUseCase {
    // Orchestration logic
}
```

#### 4. Add Infrastructure Adapters
```java
// Infrastructure layer - REST controller
@RestController
@RequestMapping("/api/v1/new-aggregates")
public class NewAggregateController {
    // HTTP endpoints
}
```

#### 5. Add Database Migration
```sql
-- src/main/resources/db/migration/V{version}__Description.sql
CREATE TABLE new_aggregates (...);
```

#### 6. Write Tests
- Domain tests (unit tests for business logic)
- Application tests (use case tests with mocks)
- Integration tests (API tests)
- Architecture tests (update ArchUnit tests)

## Coding Standards

### Java Code Style
- Follow Java naming conventions
- Use meaningful variable and method names from domain language
- Keep methods small and focused (max 20 lines preferred)
- Use Java 17 features (records, pattern matching, etc.)

### Package Naming
- `domain.*` - No Spring annotations, pure Java
- `application.*` - Spring services, but domain-focused
- `infrastructure.*` - Spring controllers, repositories, configs

### Naming Conventions

#### Domain Layer
```java
// Aggregates: Noun, singular
public class Agent { }

// Value Objects: Descriptive noun
public class AgentCode { }

// Domain Services: {Entity}DomainService
public class AgentDomainService { }

// Domain Events: {Entity}{Action}Event
public class AgentCreatedEvent { }
```

#### Application Layer
```java
// Use Cases: {Action}{Entity}UseCase
public interface CreateAgentUseCase { }

// Commands: {Action}{Entity}Command
public record CreateAgentCommand(...) implements Command { }

// Services: {Entity}CommandService or {Entity}QueryService
public class AgentCommandService { }
```

#### Infrastructure Layer
```java
// Controllers: {Entity}Controller
public class AgentController { }

// Adapters: {Entity}{Technology}Adapter
public class AgentRepositoryAdapter { }
```

### Documentation
- All public classes and methods must have Javadoc
- Domain concepts should be clearly explained
- Use `@param`, `@return`, `@throws` tags
- Include examples for complex logic

```java
/**
 * AgentCode represents the unique business identifier for an Agent.
 * 
 * <p>Format: Alphanumeric, 6-10 characters, case-insensitive.
 * The code is stored in uppercase.</p>
 * 
 * @throws DomainException if code format is invalid
 */
public final class AgentCode implements ValueObject {
    // ...
}
```

## Testing Requirements

### Test Coverage
- Domain layer: 100% coverage (business rules are critical)
- Application layer: 90%+ coverage
- Infrastructure layer: Integration tests for key flows

### Test Types

#### Unit Tests (Domain)
```java
@Test
void shouldCreateAgentWithValidData() {
    Agent agent = Agent.create(
        AgentCode.of("AGT001"),
        "John Doe",
        "BR001",
        Channel.DIRECT
    );
    
    assertThat(agent).isNotNull();
    assertThat(agent.isActive()).isTrue();
}
```

#### Application Tests
```java
@ExtendWith(MockitoExtension.class)
class AgentCommandServiceTest {
    @Mock private AgentRepository repository;
    @InjectMocks private AgentCommandService service;
    
    @Test
    void shouldCreateAgent() {
        // Test use case with mocks
    }
}
```

#### Integration Tests
```java
@WebMvcTest(AgentController.class)
class AgentControllerTest {
    @Autowired private MockMvc mockMvc;
    
    @Test
    void shouldReturnAgentById() throws Exception {
        // Test HTTP endpoint
    }
}
```

#### Architecture Tests
```java
@Test
void domainLayerShouldNotDependOnInfrastructure() {
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .check(importedClasses);
}
```

## Pull Request Process

### Before Submitting
1. ✅ Run `mvn clean install` - must pass
2. ✅ Run `mvn test` - all tests pass
3. ✅ Check `mvn verify` - ArchUnit tests pass
4. ✅ Update documentation if needed
5. ✅ Add/update tests for your changes
6. ✅ Follow commit message guidelines

### PR Template
```markdown
## Description
Brief description of what this PR does.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Checklist
- [ ] Code follows architecture principles
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
- [ ] ArchUnit tests pass

## Related Issues
Closes #123
```

### Review Criteria
PRs will be reviewed for:
- Architecture compliance
- Code quality and readability
- Test coverage
- Documentation completeness
- Business logic correctness

## Commit Message Guidelines

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples
```
feat(domain): add agent deactivation logic

Implement business rules for deactivating agents.
Add AgentDeactivatedEvent.

Closes #45
```

```
fix(api): correct validation for agent code format

Agent codes were accepting special characters.
Updated regex to alphanumeric only.
```

```
docs(architecture): update hexagonal architecture diagram

Add missing cache layer in architecture diagram.
```

## Best Practices Checklist

### Domain Layer
- [ ] No framework annotations (@Service, @Component, etc.)
- [ ] Business logic in entities, not services
- [ ] Value objects are immutable
- [ ] Domain events published for state changes
- [ ] Self-validating objects

### Application Layer
- [ ] Use cases clearly defined as interfaces
- [ ] Commands and queries separated (CQRS)
- [ ] No business logic in application services
- [ ] Orchestration only

### Infrastructure Layer
- [ ] Controllers delegate to use cases
- [ ] Adapters implement domain ports
- [ ] Framework-specific code isolated here
- [ ] API versioning maintained

### General
- [ ] No circular dependencies
- [ ] Packages organized by feature, not layer
- [ ] Configuration externalized
- [ ] Logging appropriate (not excessive)
- [ ] No sensitive data in logs

## Questions or Help?

- Check [Documentation](../docs/README.md)
- Review [Architecture Guide](../docs/architecture/code-diagrams/code-diagrams.md)
- Consult [Best Practices](../docs/development/best-practices.md)
- Ask in team chat or create an issue

Thank you for contributing!
