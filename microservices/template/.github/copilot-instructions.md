# GitHub Copilot Instructions for AI Digital Signage Microservices Template

## Project Architecture Overview

This is a **Spring Boot microservice template** following **Package by Component** architecture based on Herberto Graca's Explicit Architecture principles. It combines **Domain-Driven Design (DDD)**, **Hexagonal Architecture**, and **CQRS** patterns.

**Deployment Platform**: AWS FCP/SCC+ (Future Cloud Platform/Smart Cloud Connect+) with hybrid cloud architecture and network segregation security zoning.

### Core Principles

1. **Package by Component**: Organize code by business capabilities (e.g., `agent`, `policy`, `claim`), not technical layers
2. **Shared Kernel**: Common abstractions and infrastructure in `common` module
3. **Component Isolation**: Each component is self-contained with its own domain, application, and infrastructure layers
4. **Clean Dependencies**: Components depend only on their own packages + shared kernel from `common` (never on other components)
5. **Environment Separation**: Support for dev, UAT (UATBAU/UATPROJ/PREPROD), and production environments

---

## Technology Stack (Allianz FCP/SCC+)

### Cloud Infrastructure (AWS)
- **AWS EKS** - Kubernetes orchestration with karpenter.sh for node provisioning
- **Istio** - Service mesh for traffic management, security, and observability
- **AWS API Gateway** - API management and routing
- **AWS CloudFront** - Content Delivery Network
- **Akamai WAF (SCC+)** - Web application firewall

### Container & Registry
- **Global Registry** - Allianz group container repository
- **AWS ECR** - Elastic Container Registry

### Data Layer
- **AWS RDS Aurora PostgreSQL (SCC+)** - Primary database (Multi-AZ for UAT/prod)
- **AWS ElastiCache Redis** - Distributed caching (Cluster mode for prod)
- **Confluent Kafka (SCC+)** - Event streaming with SASL_SSL security
- **H2 Database** - In-memory database for local development

### Security & Key Management
- **AWS KMS** - Customer-managed encryption keys (CMK)
- **AWS Secrets Manager** - Credential and secret rotation
- **TLS 1.3+** - Data in transit encryption
- **Bastion/Jump Host (EC2)** - Secure access with RedHat Enterprise Linux

### Monitoring & Logging
- **Dynatrace (SCC+)** - Production APM and monitoring
- **AWS CloudWatch** - Non-production logging (logs to S3)
- **Prometheus/Micrometer** - Metrics collection
- **Spring Boot Actuator** - Health checks and management endpoints

### DevOps & IaC
- **Terraform** - Infrastructure as Code
- **GitHub Enterprise** - Source control
- **Jenkins** - CI/CD pipeline
- **Nexus Repository** - Artifact management
- **SonarQube** - Code quality and security scanning

### High Availability
- **Production**: Multi-AZ (2 zones) + Multi-AZ Disaster Recovery
- **UAT**: Multi-AZ (2 zones)
- **Development**: Single AZ

---

## Package Structure

```
io.jeecloud.aidigitalsignage/
├── agent/                          # Agent component (business capability)
│   ├── domain/                     # Business logic & rules
│   │   ├── Agent.java             # Aggregate root
│   │   ├── AgentId.java           # Value object
│   │   ├── AgentCode.java         # Value object (shareable)
│   │   ├── AgentRepository.java   # Port (interface)
│   │   ├── event/                 # Domain events
│   │   └── exception/             # Domain exceptions
│   ├── application/                # Use cases & orchestration
│   │   ├── port/
│   │   │   ├── in/                # Input ports (use case interfaces)
│   │   │   └── out/               # (Use shared ports instead)
│   │   ├── service/               # Use case implementations
│   │   ├── dto/                   # DTOs for application layer
│   │   └── mapper/                # Domain ↔ DTO mappers
│   └── infrastructure/             # Technical implementations
│       ├── rest/                  # REST controllers (adapters)
│       ├── persistence/           # JPA entities & repositories
│       ├── messaging/             # Kafka consumers/producers
│       └── cache/                 # Cache adapters
│
├── user/                           # User component (business capability)
│   ├── domain/                     # Business logic & rules
│   │   ├── User.java              # Aggregate root
│   │   ├── UserId.java            # Value object
│   │   ├── NewNric.java           # Value object
│   │   ├── UserRepository.java    # Port (interface)
│   │   ├── event/                 # Domain events
│   │   └── exception/             # Domain exceptions
│   ├── application/                # Use cases & orchestration
│   │   ├── port/in/               # Input ports (use case interfaces)
│   │   ├── service/               # Use case implementations
│   │   ├── dto/                   # DTOs for application layer
│   │   └── mapper/                # Domain ↔ DTO mappers
│   └── infrastructure/             # Technical implementations
│       ├── rest/                  # REST controllers (adapters)
│       ├── persistence/           # JPA entities & repositories
│       │   ├── UserEntity.java    # JPA entity
│       │   ├── UserAgentEntity.java  # Junction table for many-to-many
│       │   └── UserRepositoryAdapter.java
│       ├── messaging/             # Kafka consumers/producers
│       └── cache/                 # Cache adapters
│
└── SatMicroservicesTemplateApplication.java  # Spring Boot main class

**Note**: Shared kernel components are located in the separate `common` module:

common (Maven module):
├── domain/
│   ├── base/                      # Entity, ValueObject, AggregateRoot, DomainEvent
│   └── exception/                 # DomainException
├── application/
│   ├── common/                    # Command, Query, ApplicationEvent
│   └── port/out/                  # EventPublisherPort, CachePort
└── infrastructure/
    ├── config/                    # Spring configurations
    ├── rest/common/               # ApiResponse, PageRequest, PageResponse
    ├── messaging/                 # Generic Kafka adapters
    ├── cache/                     # Generic cache adapters
    └── health/                    # Health indicators
```

---

## Creating a New Component

When asked to create a new business capability (e.g., "policy", "claim", "commission"):

### Step 1: Create Directory Structure

```
io.jeecloud.aidigitalsignage.{component}/
├── domain/
│   ├── event/
│   └── exception/
├── application/
│   ├── port/
│   │   ├── in/              # Use case interfaces (Commands/Queries)
│   │   └── out/             # Output ports (Repository, EventPublisher, Cache)
│   ├── service/             # Command & Query services (CQRS)
│   ├── dto/                 # Request/Response DTOs
│   └── mapper/              # Domain ↔ DTO mappers
└── infrastructure/
    ├── rest/
    ├── persistence/
    ├── messaging/
    └── cache/
```

### Step 2: Domain Layer (Business Logic)

**Aggregate Root Pattern:**
```java
package io.jeecloud.aidigitalsignage.{component}.domain;

import io.jeecloud.aidigitalsignage.common.domain.base.AggregateRoot;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;

public class {Component} implements AggregateRoot<{Component}Id> {
    private final {Component}Id id;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    // Business methods that enforce invariants
    public void performBusinessAction() {
        // Validate business rules
        if (!isValid()) {
            throw new DomainException("Business rule violated");
        }
        
        // Perform action
        // ...
        
        // Publish domain event
        registerEvent(new {Component}ActionPerformedEvent(...));
    }
    
    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
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

**Value Object Pattern:**
```java
package io.jeecloud.aidigitalsignage.{component}.domain;

import io.jeecloud.aidigitalsignage.common.domain.base.ValueObject;
import io.jeecloud.aidigitalsignage.common.domain.exception.DomainException;

public final class {Component}Id implements ValueObject {
    private final UUID value;
    
    private {Component}Id(UUID value) {
        if (value == null) {
            throw new DomainException("{Component}Id cannot be null");
        }
        this.value = value;
    }
    
    public static {Component}Id of(UUID value) {
        return new {Component}Id(value);
    }
    
    public static {Component}Id generate() {
        return new {Component}Id(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof {Component}Id)) return false;
        {Component}Id that = ({Component}Id) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

**Repository Interface (Port):**
```java
package io.jeecloud.aidigitalsignage.{component}.domain;

import java.util.List;
import java.util.Optional;

public interface {Component}Repository {
    {Component} save({Component} {component});
    Optional<{Component}> findById({Component}Id id);
    List<{Component}> findAll();
    void deleteById({Component}Id id);
    boolean existsById({Component}Id id);
}
```

**Domain Events:**
```java
package io.jeecloud.aidigitalsignage.{component}.domain.event;

import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public class {Component}CreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID {component}Id;
    private final Instant occurredOn;
    
    public {Component}CreatedEvent(UUID {component}Id) {
        this.eventId = UUID.randomUUID();
        this.{component}Id = {component}Id;
        this.occurredOn = Instant.now();
    }
    
    @Override
    public UUID getEventId() { return eventId; }
    
    @Override
    public Instant getOccurredOn() { return occurredOn; }
    
    @Override
    public String getEventType() {
        return "{component}.created";
    }
    
    public UUID get{Component}Id() { return {component}Id; }
}
```

### Step 3: Application Layer (Use Cases & CQRS)

**Input Port - Use Case Interface:**
```java
package io.jeecloud.aidigitalsignage.{component}.application.port.in;

import io.jeecloud.aidigitalsignage.common.application.common.Command;
import io.jeecloud.aidigitalsignage.{component}.domain.{Component};

public interface Create{Component}UseCase {
    {Component} create{Component}(Create{Component}Command command);
    
    record Create{Component}Command(
        String field1,
        String field2
    ) implements Command {
        public Create{Component}Command {
            // Validation in compact constructor
            if (field1 == null || field1.isBlank()) {
                throw new IllegalArgumentException("Field1 is required");
            }
        }
        
        @Override
        public void validate() {
            // Already validated in compact constructor
        }
    }
}
```

**Output Ports:**
```java
// Event Publisher Port
package io.jeecloud.aidigitalsignage.{component}.application.port.out;

import io.jeecloud.aidigitalsignage.{component}.domain.event.*;

public interface {Component}EventPublisher {
    void publish{Component}Created({Component}CreatedEvent event);
    void publish{Component}Updated({Component}UpdatedEvent event);
}

// Cache Port
package io.jeecloud.aidigitalsignage.{component}.application.port.out;

import io.jeecloud.aidigitalsignage.{component}.domain.{Component};
import io.jeecloud.aidigitalsignage.{component}.domain.{Component}Id;
import java.util.Optional;

public interface {Component}CachePort {
    void save({Component} {component});
    Optional<{Component}> findById({Component}Id id);
    void invalidate({Component}Id id);
    void clearAll();
}
```

**Command Service (CQRS - Write Side):**
```java
package io.jeecloud.aidigitalsignage.{component}.application.service;

import io.jeecloud.aidigitalsignage.{component}.application.port.in.Create{Component}UseCase;
import io.jeecloud.aidigitalsignage.{component}.application.port.out.{Component}CachePort;
import io.jeecloud.aidigitalsignage.{component}.application.port.out.{Component}EventPublisher;
import io.jeecloud.aidigitalsignage.{component}.domain.*;
import io.jeecloud.aidigitalsignage.common.domain.base.DomainEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class {Component}CommandService implements Create{Component}UseCase {
    
    private final {Component}Repository repository;
    private final {Component}EventPublisher eventPublisher;
    private final {Component}CachePort cachePort;
    
    public {Component}CommandService(
        {Component}Repository repository,
        {Component}EventPublisher eventPublisher,
        {Component}CachePort cachePort
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.cachePort = cachePort;
    }
    
    @Override
    public {Component} create{Component}(Create{Component}Command command) {
        command.validate();
        
        // Create domain object
        {Component} {component} = {Component}.create(
            {Component}Id.generate(),
            command.field1(),
            command.field2()
        );
        
        // Save
        {Component} saved = repository.save({component});
        
        // Publish domain events
        publishDomainEvents(saved);
        
        return saved;
    }
    
    private void publishDomainEvents({Component} {component}) {
        for (DomainEvent event : {component}.getDomainEvents()) {
            if (event instanceof {Component}CreatedEvent createdEvent) {
                eventPublisher.publish{Component}Created(createdEvent);
            } else if (event instanceof {Component}UpdatedEvent updatedEvent) {
                eventPublisher.publish{Component}Updated(updatedEvent);
            }
        }
        {component}.clearDomainEvents();
    }
}
```

**Query Service (CQRS - Read Side):**
```java
package io.jeecloud.aidigitalsignage.{component}.application.service;

import io.jeecloud.aidigitalsignage.{component}.application.port.in.Find{Component}UseCase;
import io.jeecloud.aidigitalsignage.{component}.application.port.out.{Component}CachePort;
import io.jeecloud.aidigitalsignage.{component}.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class {Component}QueryService implements Find{Component}UseCase {
    
    private final {Component}Repository repository;
    private final {Component}CachePort cachePort;
    
    public {Component}QueryService(
        {Component}Repository repository,
        {Component}CachePort cachePort
    ) {
        this.repository = repository;
        this.cachePort = cachePort;
    }
    
    @Override
    public Optional<{Component}> findById(FindByIdQuery query) {
        {Component}Id id = {Component}Id.of(UUID.fromString(query.id()));
        
        // Try cache first
        Optional<{Component}> cached = cachePort.findById(id);
        if (cached.isPresent()) {
            return cached;
        }
        
        // Cache miss - query repository
        Optional<{Component}> {component} = repository.findById(id);
        
        // Update cache if found
        {component}.ifPresent(cachePort::save);
        
        return {component};
    }
}
```

**DTO:**
```java
package io.jeecloud.aidigitalsignage.{component}.application.dto;

import io.jeecloud.aidigitalsignage.{component}.domain.{Component};

public record {Component}Response(
    UUID id,
    String field1,
    String field2,
    Instant createdAt,
    Instant updatedAt
) {
    public static {Component}Response from({Component} {component}) {
        return new {Component}Response(
            {component}.getId().getValue(),
            {component}.getField1(),
            {component}.getField2(),
            {component}.getCreatedAt(),
            {component}.getUpdatedAt()
        );
    }
}
```

### Step 4: Infrastructure Layer (Adapters)

**REST Controller:**
```java
package io.jeecloud.aidigitalsignage.{component}.infrastructure.rest;

import io.jeecloud.aidigitalsignage.{component}.application.port.in.*;
import io.jeecloud.aidigitalsignage.{component}.application.dto.*;
import io.jeecloud.aidigitalsignage.common.infrastructure.rest.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{components}")
public class {Component}Controller {
    
    private final Create{Component}UseCase create{Component}UseCase;
    private final Find{Component}UseCase find{Component}UseCase;
    
    public {Component}Controller(
        Create{Component}UseCase create{Component}UseCase,
        Find{Component}UseCase find{Component}UseCase
    ) {
        this.create{Component}UseCase = create{Component}UseCase;
        this.find{Component}UseCase = find{Component}UseCase;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<{Component}Response>> create(
        @RequestBody @Valid {Component}Request request
    ) {
        var command = new Create{Component}UseCase.Create{Component}Command(
            request.field1(),
            request.field2()
        );
        
        {Component}Response response = create{Component}UseCase.create(command);
        
        return ResponseEntity
            .created(URI.create("/api/v1/{components}/" + response.id()))
            .body(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<{Component}Response> getById(@PathVariable UUID id) {
        return find{Component}UseCase.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

**JPA Entity (Infrastructure):**
```java
package io.jeecloud.aidigitalsignage.{component}.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "{components}")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {Component}Entity {
    
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    
    @Column(nullable = false)
    private String field1;
    
    @Column(nullable = false)
    private String field2;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

**Repository Adapter:**
```java
package io.jeecloud.aidigitalsignage.{component}.infrastructure.persistence;

import io.jeecloud.aidigitalsignage.{component}.domain.*;
import io.jeecloud.aidigitalsignage.{component}.domain.exception.{Component}NotFoundException;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class {Component}RepositoryAdapter implements {Component}Repository {
    
    private final {Component}JpaRepository jpaRepository;
    
    public {Component}RepositoryAdapter({Component}JpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public {Component} save({Component} {component}) {
        {Component}Entity entity = toEntity({component});
        {Component}Entity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public Optional<{Component}> findById({Component}Id id) {
        return jpaRepository.findById(id.getValue())
            .map(this::toDomain);
    }
    
    @Override
    public List<{Component}> findAll() {
        return jpaRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById({Component}Id id) {
        jpaRepository.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsById({Component}Id id) {
        return jpaRepository.existsById(id.getValue());
    }
    
    // Mapping methods
    private {Component}Entity toEntity({Component} domain) {
        return {Component}Entity.builder()
            .id(domain.getId().getValue())
            .field1(domain.getField1())
            .field2(domain.getField2())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
    
    private {Component} toDomain({Component}Entity entity) {
        return {Component}.reconstitute(
            {Component}Id.of(entity.getId()),
            entity.getField1(),
            entity.getField2(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
```

---

## Architecture Rules (MUST FOLLOW)

### 1. Dependency Direction
```
Infrastructure → Application → Domain
         ↓
   common module (Shared Kernel)
```

**ALLOWED:**
- ✅ Infrastructure can depend on Application and Domain
- ✅ Application can depend on Domain
- ✅ All layers can depend on `common` module (Shared Kernel)
- ✅ Components can depend on `common` module (Shared Kernel)

**FORBIDDEN:**
- ❌ Domain cannot depend on Application or Infrastructure
- ❌ Application cannot depend on Infrastructure
- ❌ Components cannot depend on other components
- ❌ `common` module cannot depend on components

### 2. Package Rules

**DO:**
- ✅ Place business logic in `domain/`
- ✅ Place use cases in `application/service/`
- ✅ Place technical adapters in `infrastructure/`
- ✅ Place cross-cutting concerns in `common` module
- ✅ Use interfaces (ports) for external dependencies
- ✅ Keep entities rich with behavior (not anemic)

**DON'T:**
- ❌ Put Spring annotations in Domain layer (except `@Service` for domain services)
- ❌ Put database annotations in Domain entities
- ❌ Mix different components' code
- ❌ Create circular dependencies
- ❌ Bypass repository interfaces in application layer

### 3. Naming Conventions

- **Components**: Lowercase, plural for packages: `agent/`, `policy/`, `claim/`
- **Aggregates**: PascalCase, singular: `Agent`, `Policy`, `Claim`
- **Value Objects**: PascalCase with suffix: `AgentId`, `PolicyNumber`, `ClaimStatus`
- **Use Cases**: Interface with "UseCase" suffix: `CreateAgentUseCase`
- **Services**: Impl with "Service" suffix: `AgentCommandService`, `AgentQueryService`
- **DTOs**: Suffix with Request/Response: `AgentRequest`, `AgentResponse`
- **Entities (JPA)**: Suffix with "Entity": `AgentEntity`, `PolicyEntity`
- **Repositories (JPA)**: Suffix with "JpaRepository": `AgentJpaRepository`
- **Adapters**: Suffix with "Adapter": `AgentRepositoryAdapter`, `KafkaEventPublisher`

### 4. CQRS Pattern

Separate **Commands** (write) from **Queries** (read):

```java
// Commands - modify state
public interface CreateAgentUseCase { ... }
public interface UpdateAgentUseCase { ... }
public interface DeleteAgentUseCase { ... }

// Implement in: AgentCommandService

// Queries - read state
public interface FindAgentUseCase { ... }

// Implement in: AgentQueryService
```

### 5. Event-Driven Communication

**Domain Events** for internal business changes:
```java
// Published by aggregate root
agent.activate(); // triggers AgentActivatedEvent
```

**Application Events** for technical concerns:
```java
// Published by application service
applicationEventPublisher.publish(new CacheInvalidatedEvent(...));
```

---

## Testing Guidelines

### Unit Tests (Domain)
```java
@Test
void shouldCreateValidAgent() {
    Agent agent = Agent.create(
        AgentId.generate(),
        AgentCode.of("AG001"),
        "John Doe"
    );
    
    assertThat(agent.getName()).isEqualTo("John Doe");
    assertThat(agent.getDomainEvents()).hasSize(1);
    assertThat(agent.getDomainEvents().get(0))
        .isInstanceOf(AgentCreatedEvent.class);
}
```

### Integration Tests (Application)
```java
@SpringBootTest
@Transactional
class AgentCommandServiceTest {
    @Autowired
    private AgentCommandService service;
    
    @Test
    void shouldCreateAgent() {
        var command = new CreateAgentCommand("AG001", "John Doe");
        AgentResponse response = service.create(command);
        
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("John Doe");
    }
}
```

### API Tests (Infrastructure)
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class AgentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateAgent() throws Exception {
        mockMvc.perform(post("/api/v1/agents")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "agentCode": "AG001",
                    "name": "John Doe"
                }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

---

## Common Patterns

### 1. Validation
- **Domain validation**: In constructors/factory methods
- **DTO validation**: Use Bean Validation annotations (`@NotNull`, `@Size`)
- **Command validation**: In `Command.validate()` method

### 2. Exception Handling
```java
// Domain exceptions
throw new DomainException("Business rule violated");

// Not found exceptions (extend DomainException)
throw new AgentNotFoundException(agentId);

// Global exception handler in infrastructure/rest
@ControllerAdvice
public class GlobalExceptionHandler { ... }
```

### 3. Caching Strategy
```java
// In query service
String cacheKey = "agent:" + agentId;
Optional<Agent> cached = cachePort.get(cacheKey, Agent.class);
if (cached.isPresent()) return cached.get();

Agent agent = repository.findById(agentId);
cachePort.put(cacheKey, agent, 3600); // TTL in seconds
return agent;
```

### 4. Event Publishing
```java
// After saving aggregate
agent.getDomainEvents().forEach(eventPublisher::publish);
agent.clearDomainEvents();
```

---

## When to Use common vs Component

### Use common Module (Shared Kernel) for:
- ✅ Base interfaces (`Entity`, `ValueObject`, `AggregateRoot`)
- ✅ Common exceptions (`DomainException`)
- ✅ Generic ports (`EventPublisherPort`, `CachePort`)
- ✅ Infrastructure configs (Kafka, Redis, Web, etc.)
- ✅ Common REST wrappers (`ApiResponse`, `PageResponse`)
- ✅ Health checks

### Use Component Package for:
- ✅ Business entities specific to that component
- ✅ Component-specific value objects
- ✅ Component-specific repositories
- ✅ Component-specific use cases
- ✅ Component-specific DTOs
- ✅ Component-specific controllers

---

## Quick Reference: File Locations

| What | Where | Example |
|------|-------|---------|
| Aggregate Root | `{component}/domain/` | `Agent.java` |
| Value Object | `{component}/domain/` | `AgentId.java`, `AgentCode.java` |
| Repository Interface | `{component}/domain/` | `AgentRepository.java` |
| Domain Event | `{component}/domain/event/` | `AgentCreatedEvent.java` |
| Domain Exception | `{component}/domain/exception/` | `AgentNotFoundException.java` |
| Use Case Interface | `{component}/application/port/in/` | `CreateAgentUseCase.java` |
| Service Implementation | `{component}/application/service/` | `AgentCommandService.java` |
| DTO | `{component}/application/dto/` | `AgentRequest.java`, `AgentResponse.java` |
| Mapper | `{component}/application/mapper/` | `AgentMapper.java` |
| REST Controller | `{component}/infrastructure/rest/` | `AgentController.java` |
| JPA Entity | `{component}/infrastructure/persistence/` | `AgentEntity.java` |
| JPA Repository | `{component}/infrastructure/persistence/` | `AgentJpaRepository.java` |
| Repository Adapter | `{component}/infrastructure/persistence/` | `AgentRepositoryAdapter.java` |
| Kafka Consumer | `{component}/infrastructure/messaging/` | `{Component}EventConsumer.java` |
| Base Interfaces (common) | `domain/base/` | `Entity.java`, `AggregateRoot.java` |
| Common Ports (common) | `application/port/out/` | `EventPublisherPort.java` |
| Configuration (common) | `infrastructure/config/` | `KafkaConfig.java`, `RedisConfig.java` |

---

## Environment-Specific Configuration

### Development Environment (`dev` profile)
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb
  kafka:
    bootstrap-servers: localhost:9092
  data:
    redis:
      host: localhost
      port: 6379
```

**Usage**: Local development with H2 in-memory database, local Kafka and Redis instances.

### UAT Environment (`uat` profile)
```yaml
spring:
  profiles:
    active: uat
  datasource:
    url: ${DB_URL}  # Aurora PostgreSQL Multi-AZ
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}  # Confluent Kafka (SCC+)
    properties:
      security.protocol: SASL_SSL
      sasl.mechanism: PLAIN
  data:
    redis:
      host: ${REDIS_HOST}  # ElastiCache Redis
      ssl:
        enabled: true
aws:
  secrets:
    enabled: true
    secret-name: ai-ds-service-uat
  cloudwatch:
    enabled: true
```

**Usage**: User acceptance testing environments (UATBAU, UATPROJ, PREPROD) with AWS managed services and CloudWatch logging.

### Production Environment (`prod` profile)
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: ${DB_URL}  # Aurora PostgreSQL Multi-AZ + Read Replicas
    hikari:
      maximum-pool-size: 50
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}  # Confluent Kafka (SCC+)
    producer:
      compression-type: lz4
    properties:
      security.protocol: SASL_SSL
  data:
    redis:
      cluster:
        nodes: ${REDIS_CLUSTER_NODES}  # ElastiCache Redis Cluster
      ssl:
        enabled: true
aws:
  kms:
    key-id: ${KMS_KEY_ID}  # Customer-managed keys
  secrets:
    enabled: true
    secret-name: ai-ds-service-prod
dynatrace:
  enabled: true  # Dynatrace APM for production monitoring
security:
  ssl:
    protocol: TLSv1.3
```

**Usage**: Production workloads with enhanced security (KMS, Secrets Manager), Dynatrace monitoring, Redis clustering, and Multi-AZ DR.

**Configuration Files:**
- `application.yml` - Base configuration
- `application-dev.yml` - Development overrides
- `application-uat.yml` - UAT overrides
- `application-prod.yml` - Production overrides

---

## AI Generation Tips

When generating code:
1. **Always ask**: "What is the business capability/component name?"
2. **Generate complete vertical slice**: Domain → Application → Infrastructure
3. **Follow naming conventions** strictly
4. **Include validation** in domain and commands
5. **Add logging** (SLF4J) in services and controllers
6. **Use records** for DTOs and commands
7. **Include Javadoc** for public APIs
8. **Test-first**: Generate tests alongside implementation
9. **Check dependencies**: Ensure no circular or forbidden dependencies
10. **Use Lombok** judiciously (Builder, Getter/Setter for entities only)
11. **Consider environment**: Use externalized configuration with `${VARIABLE}` for environment-specific values
12. **Security first**: Always use AWS Secrets Manager for sensitive data in UAT/prod

---

## Examples in This Template

Reference these components as canonical examples:

### Agent Component (Complete CRUD)
- **Purpose**: Demonstrates single aggregate with full CRUD operations
- Domain: [Agent.java](src/main/java/com/allianz/sat/agent/domain/Agent.java)
- Use Case: [CreateAgentUseCase.java](src/main/java/com/allianz/sat/agent/application/port/in/CreateAgentUseCase.java)
- Service: [AgentCommandService.java](src/main/java/com/allianz/sat/agent/application/service/AgentCommandService.java)
- Controller: [AgentController.java](src/main/java/com/allianz/sat/agent/infrastructure/rest/AgentController.java)
- Persistence: [AgentRepositoryAdapter.java](src/main/java/com/allianz/sat/agent/infrastructure/persistence/AgentRepositoryAdapter.java)

### User Component (Many-to-Many Relationships)
- **Purpose**: Demonstrates component relationships and junction tables
- Domain: [User.java](src/main/java/com/allianz/sat/user/domain/User.java)
- Junction Entity: [UserAgentEntity.java](src/main/java/com/allianz/sat/user/infrastructure/persistence/UserAgentEntity.java)
- Repository: [UserRepositoryAdapter.java](src/main/java/com/allianz/sat/user/infrastructure/persistence/UserRepositoryAdapter.java)
- **Key Pattern**: User references AgentCode (value object), not full Agent aggregate
- **Demonstrates**: Loose coupling between components via shared value objects

---

## Architecture Validation

This template includes ArchUnit tests to enforce architecture rules. Run:
```bash
mvn test -Dtest=ArchitectureTest
```

**Key Rules Enforced:**
1. No cycles between packages
2. Components don't depend on each other
3. Domain doesn't depend on Application/Infrastructure
4. Application doesn't depend on Infrastructure
5. All layers can depend on Shared Kernel
6. Naming conventions are followed

---

## Documentation

- [Architecture Code Diagrams](docs/architecture/code-diagrams/code-diagrams.md) - Full architectural explanation
- [README](README.md) - Project overview and setup
- [AI Digital Signage Menu](docs/allianz-sat-menu-structure.md) - Business context

---

**Remember**: This is a **TEMPLATE**. When creating a new microservice:
1. Clone this repository
2. Rename packages (`sat` → your service name)
3. Remove example `agent/` component
4. Add your business components following patterns above
5. Update documentation

**Stay true to the architecture!** Package by Component + DDD + Hexagonal + CQRS = Maintainable, scalable microservices. 🚀
