# AI Digital Signage Shared Library

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-green)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red)](https://maven.apache.org/)

> **Shared Kernel Library** for AI Digital Signage microservices ecosystem. Contains reusable domain patterns, application components, and infrastructure configurations.

---

## 📦 What's Included

### 1. Domain Base Interfaces (`io.jeecloud.aidigitalsignage.common.domain.base`)
Building blocks for Domain-Driven Design:
- **`Entity<ID>`** - Base interface for all entities with identity
- **`AggregateRoot<ID>`** - Root entities that maintain consistency boundaries
- **`ValueObject`** - Marker interface for immutable value objects
- **`DomainEvent`** - Base interface for domain events

### 2. Domain Exceptions (`io.jeecloud.aidigitalsignage.common.domain.exception`)
- **`DomainException`** - Base exception for domain-level errors

### 3. Application CQRS Patterns (`io.jeecloud.aidigitalsignage.common.application.common`)
- **`Command`** - Marker interface for write operations
- **`Query`** - Marker interface for read operations
- **`ApplicationEvent`** - Base for application-level events

### 4. Output Ports (`io.jeecloud.aidigitalsignage.common.application.port.out`)
Generic port interfaces following Hexagonal Architecture:
- **`EventPublisherPort<T>`** - Event publishing abstraction
- **`CachePort<K, V>`** - Caching abstraction

### 5. Infrastructure Configurations (`io.jeecloud.aidigitalsignage.common.infrastructure.config`)
Ready-to-use Spring configurations:
- **`KafkaConfig`** - Kafka producer/consumer setup
- **`RedisConfig`** - Redis caching configuration
- **`WebConfig`** - CORS, interceptors, filters
- **`OpenApiConfig`** - Swagger/OpenAPI documentation
- **`ObservabilityConfig`** - Metrics, tracing, monitoring
- **`ApplicationProperties`** - Common application properties

### 6. Messaging Infrastructure (`io.jeecloud.aidigitalsignage.common.infrastructure.messaging`)
- **`KafkaEventPublisher`** - Generic Kafka event publisher
- **`KafkaEventConsumer`** - Base Kafka consumer

### 7. REST Common Types (`io.jeecloud.aidigitalsignage.common.infrastructure.rest.common`)
Standardized REST API responses:
- **`ApiResponse<T>`** - Wrapper for consistent API responses
- **`ErrorResponse`** - Error response structure
- **`PageRequest`** - Pagination request parameters
- **`PageResponse<T>`** - Paginated response wrapper

### 8. Health Checks (`io.jeecloud.aidigitalsignage.common.infrastructure.health`)
- Custom health indicators for microservices

---

## 🚀 Usage

### Add as Maven Dependency

```xml
<dependency>
    <groupId>io.jeecloud.aidigitalsignage</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Example: Creating a Domain Entity

```java
import io.jeecloud.aidigitalsignage.common.domain.base.AggregateRoot;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;

public class Agent implements AggregateRoot<AgentId> {
    private AgentId id;
    private String name;
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    @Override
    public AgentId getId() {
        return id;
    }
    
    @Override
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    @Override
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

### Example: Creating a Command

```java
import io.jeecloud.aidigitalsignage.common.application.common.Command;

public record CreateAgentCommand(String code, String name) implements Command {
    @Override
    public void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Agent code is required");
        }
    }
}
```

### Example: Using Output Ports

```java
import io.jeecloud.aidigitalsignage.common.application.port.out.EventPublisherPort;

@Service
public class AgentCommandService {
    private final EventPublisherPort<DomainEvent> eventPublisher;
    
    public AgentCommandService(EventPublisherPort<DomainEvent> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void createAgent(CreateAgentCommand command) {
        // Business logic...
        AgentCreatedEvent event = new AgentCreatedEvent(agent);
        eventPublisher.publish(event);
    }
}
```

### Example: Using REST Common Types

```java
import io.jeecloud.aidigitalsignage.common.infrastructure.rest.common.ApiResponse;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    
    @GetMapping("/{id}")
    public ApiResponse<AgentResponse> getAgent(@PathVariable String id) {
        AgentResponse agent = agentService.findById(id);
        return ApiResponse.success(agent);
    }
}
```

---

## 🏗️ Architecture Principles

This library follows **Explicit Architecture** principles:

1. **Dependency Rule**: Infrastructure → Application → Domain
2. **Clean Boundaries**: Clear separation of concerns
3. **Reusability**: Generic abstractions for common patterns
4. **Framework Independence**: Domain layer has no framework dependencies
5. **Testability**: Easy to mock and test

---

## 📋 Requirements

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.2 or higher
- **Maven**: 3.8+

---

## 🔧 Building the Library

```bash
# Build the shared library
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Deploy to repository
mvn clean deploy
```

---

## 📖 Library Structure

```
common/
├── pom.xml                                         # Maven configuration
├── README.md                                       # This documentation
└── src/main/java/com/allianz/sat/common/
    ├── domain/                                     # Domain Layer (business logic)
    │   ├── base/                                   # Core DDD building blocks
    │   │   ├── Entity.java                         # Base interface for entities
    │   │   ├── AggregateRoot.java                  # Root entity interface
    │   │   ├── ValueObject.java                    # Value object marker
    │   │   └── DomainEvent.java                    # Domain event interface
    │   └── exception/                              # Domain exceptions
    │       └── DomainException.java                # Base domain exception
    ├── application/                                # Application Layer (use cases)
    │   ├── common/                                 # CQRS pattern interfaces
    │   │   ├── Command.java                        # Write operation marker
    │   │   ├── Query.java                          # Read operation marker
    │   │   └── ApplicationEvent.java               # Application event base
    │   └── port/out/                               # Output ports (dependencies)
    │       ├── EventPublisherPort.java             # Event publishing abstraction
    │       └── CachePort.java                      # Caching abstraction
    └── infrastructure/                             # Infrastructure Layer (technical)
        ├── config/                                 # Spring Boot configurations
        │   ├── KafkaConfig.java                    # Kafka setup
        │   ├── RedisConfig.java                    # Redis caching setup
        │   ├── WebConfig.java                      # Web/CORS configuration
        │   └── OpenApiConfig.java                  # Swagger/API docs
        ├── messaging/                              # Event messaging
        │   ├── KafkaEventPublisher.java            # Kafka publisher impl
        │   └── KafkaEventConsumer.java             # Kafka consumer base
        ├── rest/common/                            # REST API standards
        │   ├── ApiResponse.java                    # Standard response wrapper
        │   ├── ErrorResponse.java                  # Error response structure
        │   ├── PageRequest.java                    # Pagination request
        │   └── PageResponse.java                   # Pagination response
        └── health/                                 # Health checks
            └── CustomHealthIndicator.java          # Custom health indicators
```

## 📚 Integration with Microservices

### Project Structure
```
your-microservice/
├── pom.xml  (depends on common)
└── src/main/java/com/your/service/
    ├── agent/
    │   ├── domain/           # Uses common.domain.base.*
    │   ├── application/      # Uses common.application.common.*
    │   └── infrastructure/   # Uses common.infrastructure.*
    └── Application.java
```

### Dependency Injection
All shared infrastructure beans are auto-configured via Spring Boot auto-configuration.

---

## 🎯 Best Practices

### ✅ DO:
- Use shared domain base interfaces for all entities/aggregates
- Implement shared CQRS patterns (Command/Query)
- Reuse shared infrastructure configurations
- Extend shared exception types
- Use shared REST response types for consistency

### ❌ DON'T:
- Add business-specific domain logic to this library
- Create dependencies between microservices through shared code
- Override shared configurations without careful consideration
- Mix infrastructure concerns in domain layer

---

## 📖 Documentation

For more details on architecture and patterns:
- [Architecture Code Diagrams](../docs/architecture/code-diagrams/code-diagrams.md)
- [Best Practices](../docs/development/best-practices.md)
- [Development Guide](../docs/development/development-guide.md)

---

## 🤝 Contributing

This is a shared library used across multiple microservices. Changes should be:
1. Backward compatible when possible
2. Well-tested with unit tests
3. Documented with JavaDoc
4. Reviewed by architecture team

---

## 📄 License

Proprietary - Allianz Technology SE

---

## 🆘 Support

For questions or issues:
- Check existing documentation in [../template/docs](../template/docs)
- Contact the Platform Architecture team
- Create an issue in the repository

---

**Version**: 1.0.0-SNAPSHOT  
**Last Updated**: January 16, 2026
