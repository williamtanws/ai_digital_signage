# GitHub Copilot Instructions

> **AI Code Generation Guidelines** for SAT Microservices Template

---

## 🎯 Core Principles

You are assisting in a **Spring Boot microservices project** that follows:
1. **Hexagonal Architecture** (Ports & Adapters)
2. **Domain-Driven Design** (DDD)
3. **CQRS Pattern** (Command Query Responsibility Segregation)
4. **Package by Component** (NOT package by layer)
5. **Clean Architecture** dependency rules

---

## 🤝 User Interaction Guidelines (CRITICAL)

**ALWAYS ask clarifying questions BEFORE proceeding with implementation when:**

1. **Requirements are ambiguous or incomplete**
   - Missing field names, types, or validation rules
   - Unclear business logic or workflow
   - Undefined error handling behavior

2. **Multiple architectural approaches are possible**
   - Which component should this feature belong to?
   - Should this be a new aggregate or part of an existing one?
   - Is this a command operation, query operation, or both?

3. **Impact on existing code is unclear**
   - Will this change affect other components?
   - Should existing endpoints/services be modified or deprecated?
   - Are there backward compatibility concerns?

4. **Technology/implementation choices need validation**
   - Which caching strategy to use?
   - Should this be synchronous or asynchronous?
   - What's the expected data volume/performance requirement?

5. **Business domain knowledge is needed**
   - What are the business rules for this entity?
   - What validation constraints apply?
   - What's the expected behavior in edge cases?

### ✅ Example Good Interactions

**Scenario: User says "Create agent endpoint"**

❌ **DON'T assume:**
- Field names, types, validation rules
- Whether it's CRUD or specific operations
- Which business rules apply

✅ **DO ask:**
- "What fields should the Agent entity have?"
- "What operations do you need? (Create, Read, Update, Delete, or specific business operations?)"
- "Are there any validation rules or business constraints for agents?"
- "Should I create both command and query services following CQRS?"

**Scenario: User says "Add validation"**

❌ **DON'T assume:**
- Which fields need validation
- What validation rules apply
- Where validation should occur

✅ **DO ask:**
- "Which fields need validation?"
- "What are the validation rules? (e.g., required, format, length, business constraints)"
- "Should this be bean validation (@Valid) or domain validation in entities?"

**Scenario: User says "Fix the repository"**

❌ **DON'T assume:**
- What the issue is
- Which repository
- What the expected behavior should be

✅ **DO ask:**
- "Which repository are you referring to?"
- "What issue are you experiencing?"
- "What's the expected behavior?"

### 🚫 Never Assume

- Field names or data types (always ask for specifications)
- Business logic or validation rules (confirm with user)
- Whether to modify existing code or create new components
- Which component a feature belongs to
- API endpoint paths or HTTP methods
- Database column names or constraints
- Whether caching, events, or async processing is needed

### 📋 Before Major Changes, Confirm

1. **Summarize your understanding** of the requirement
2. **List the changes** you plan to make (which files, what modifications)
3. **Mention any assumptions** you're making
4. **Ask for confirmation** before proceeding

Example:
> "Based on your request, I understand you want to:
> 1. Create a new Agent aggregate in the domain layer
> 2. Add AgentCode as a value object
> 3. Create CRUD operations via command and query services
> 4. Expose REST endpoints for these operations
> 
> I'll be creating files in these locations:
> - Domain: `Agent.java`, `AgentCode.java`
> - Application: `AgentCommandService.java`, `AgentQueryService.java`
> - Infrastructure: `AgentController.java`, `AgentRepositoryAdapter.java`
> 
> Should I proceed with this approach?"

---

## 🏗️ Architecture Overview

### Dependency Direction (CRITICAL)
```
Infrastructure → Application → Domain → Common
(Outer depends on Inner, NEVER reverse)
```

### Layer Responsibilities
- **Domain**: Pure business logic (NO frameworks)
- **Application**: Use cases + orchestration (minimal Spring)
- **Infrastructure**: Adapters for REST/DB/Kafka/Redis (full framework usage)

---

## 📦 Package Structure

```
com.allianz.sat/
├── agent/                    # Business Component
│   ├── domain/              # Entities, Value Objects, Events
│   ├── application/         # Use Cases, DTOs, Services (CQRS)
│   └── infrastructure/      # REST, JPA, Kafka adapters
├── user/                     # Another Component
└── common/                   # Shared Kernel (from sat-common module)
```

---

## 🔒 Layer Rules (STRICTLY ENFORCED)

### DOMAIN Layer (`*.domain.*`)

**Allowed:**
```java
✅ java.* (standard library)
✅ com.allianz.sat.common.domain.* (base interfaces)
✅ org.apache.commons.lang3.* (utilities only)
```

**FORBIDDEN:**
```java
❌ org.springframework.*
❌ jakarta.persistence.*
❌ com.fasterxml.jackson.*
❌ Any application or infrastructure imports
```

**Patterns:**
```java
// Entities with business logic
public class Agent implements AggregateRoot<AgentCode> {
    public void activate() {
        // Business validation here
        this.active = true;
        registerEvent(new AgentActivatedEvent(this.agentCode));
    }
}

// Immutable Value Objects
public record AgentCode(String value) implements ValueObject {
    public AgentCode {
        if (value == null || value.length() < 6) {
            throw new IllegalArgumentException("Invalid code");
        }
    }
}

// Repository interfaces (domain contract)
public interface AgentRepository {
    Agent save(Agent agent);
    Optional<Agent> findById(AgentCode code);
}
```

---

### APPLICATION Layer (`*.application.*`)

**Allowed:**
```java
✅ *.domain.*
✅ com.allianz.sat.common.application.*
✅ @Service, @Transactional (Spring annotations)
✅ org.slf4j.* (logging)
```

**FORBIDDEN:**
```java
❌ *.infrastructure.*
❌ org.springframework.web.*
❌ jakarta.persistence.*
❌ org.apache.kafka.*
```

**Patterns:**
```java
// Use Case Interface (Input Port)
public interface CreateAgentUseCase {
    Agent createAgent(CreateAgentCommand command);
    
    record CreateAgentCommand(
        String agentCode,
        String name
    ) implements Command { }
}

// Command Service (Write operations)
@Service
@Transactional
public class AgentCommandService implements CreateAgentUseCase, UpdateAgentUseCase {
    private final AgentRepository repository;
    private final EventPublisherPort eventPublisher;
    
    @Override
    public Agent createAgent(CreateAgentCommand command) {
        Agent agent = Agent.create(...);
        Agent saved = repository.save(agent);
        publishEvents(saved);
        return saved;
    }
}

// Query Service (Read operations + caching)
@Service
@Transactional(readOnly = true)
public class AgentQueryService implements FindAgentUseCase {
    private final AgentRepository repository;
    private final AgentCachePort cache;
    
    @Override
    public Optional<Agent> findByAgentCode(String code) {
        return cache.getCachedAgent(AgentCode.of(code))
            .or(() -> repository.findById(AgentCode.of(code)));
    }
}

// DTOs (Application layer only)
public record AgentResponse(String code, String name) {
    public static AgentResponse from(Agent agent) {
        return new AgentResponse(
            agent.getAgentCode().value(),
            agent.getName()
        );
    }
}
```

---

### INFRASTRUCTURE Layer (`*.infrastructure.*`)

**Allowed:**
```java
✅ *.domain.*
✅ *.application.*
✅ All Spring/JPA/Kafka/Redis libraries
✅ All third-party libraries
```

**Patterns:**
```java
// REST Controller (Primary Adapter)
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {
    private final CreateAgentUseCase createUseCase;
    private final FindAgentUseCase findUseCase;
    
    @PostMapping
    public ResponseEntity<AgentResponse> create(@Valid @RequestBody AgentRequest req) {
        CreateAgentCommand cmd = new CreateAgentCommand(...);
        Agent agent = createUseCase.createAgent(cmd);
        return ResponseEntity.ok(AgentResponse.from(agent));
    }
}

// Repository Adapter (Secondary Adapter)
@Repository
public class AgentRepositoryAdapter implements AgentRepository {
    private final AgentJpaRepository jpaRepo;
    
    @Override
    public Agent save(Agent agent) {
        AgentEntity entity = AgentEntity.fromDomain(agent);
        AgentEntity saved = jpaRepo.save(entity);
        return saved.toDomain();
    }
}

// JPA Repository (Infrastructure detail)
interface AgentJpaRepository extends JpaRepository<AgentEntity, String> {
    List<AgentEntity> findByActive(boolean active);
}

// JPA Entity (separate from domain)
@Entity
@Table(name = "agents")
public class AgentEntity {
    @Id
    private String agentCode;
    
    public static AgentEntity fromDomain(Agent agent) { }
    public Agent toDomain() { }
}
```

---

## 📋 Naming Conventions (MUST FOLLOW)

| Type | Pattern | Example |
|------|---------|---------|
| Use Case Interface | `*UseCase` | `CreateAgentUseCase` |
| Command Record | `*Command` | `CreateAgentCommand` |
| Domain Event | `*Event` | `AgentCreatedEvent` |
| Repository Interface | `*Repository` | `AgentRepository` |
| Repository Adapter | `*RepositoryAdapter` | `AgentRepositoryAdapter` |
| JPA Repository | `*JpaRepository` | `AgentJpaRepository` |
| JPA Entity | `*Entity` | `AgentEntity` |
| Controller | `*Controller` | `AgentController` |
| Command Service | `*CommandService` | `AgentCommandService` |
| Query Service | `*QueryService` | `AgentQueryService` |
| DTO Request | `*Request` | `AgentRequest` |
| DTO Response | `*Response` | `AgentResponse` |

---

## 🚫 Anti-Patterns to AVOID

### ❌ 1. Controller Using Repository Directly
```java
// WRONG
@RestController
public class AgentController {
    private final AgentRepository repository; // NO!
}

// CORRECT
@RestController
public class AgentController {
    private final CreateAgentUseCase createUseCase; // YES
    private final FindAgentUseCase findUseCase;     // YES
}
```

### ❌ 2. Domain with JPA Annotations
```java
// WRONG
@Entity
public class Agent { }  // NO! Domain entities are framework-free

// CORRECT
public class Agent implements AggregateRoot<AgentCode> { } // YES

@Entity
public class AgentEntity { } // YES - in infrastructure layer
```

### ❌ 3. Business Logic in Services
```java
// WRONG
@Service
public class AgentService {
    public void activateAgent(Agent agent) {
        agent.setActive(true); // Business logic in service!
    }
}

// CORRECT
public class Agent {
    public void activate() { // Business logic in entity
        if (this.active) {
            throw new DomainException("Already active");
        }
        this.active = true;
    }
}
```

### ❌ 4. Application Depending on Infrastructure
```java
// WRONG
@Service
public class AgentCommandService {
    @Autowired
    private AgentJpaRepository jpaRepo; // NO! Infrastructure dependency
}

// CORRECT
@Service
public class AgentCommandService {
    private final AgentRepository repository; // YES! Domain interface
}
```

### ❌ 5. Domain Returning DTOs
```java
// WRONG
public class Agent {
    public AgentResponse toResponse() { } // NO! Domain doesn't know DTOs
}

// CORRECT
public record AgentResponse(...) {
    public static AgentResponse from(Agent agent) { } // YES! DTO knows domain
}
```

---

## ✅ Code Generation Workflow

When generating new code, **ALWAYS follow this sequence:**

### 1. Identify the Layer
- Domain? → Pure Java, business logic
- Application? → Use cases, orchestration
- Infrastructure? → REST/DB/Kafka adapters

### 2. Check Dependencies
- Domain → ZERO external dependencies
- Application → Domain + Common only
- Infrastructure → Everything

### 3. Apply Patterns
- Domain → Rich entities, value objects, events
- Application → CQRS services, use case interfaces, DTOs
- Infrastructure → Adapters implementing ports

### 4. Follow Naming
- Use correct suffix: `UseCase`, `Command`, `Repository`, `Entity`, etc.

### 5. Separate Concerns
- Business logic → Domain entities
- Orchestration → Application services
- Technical details → Infrastructure adapters

---

## 🎯 CQRS Pattern (MUST USE)

**Command Side (Write Operations):**
```java
@Service
@Transactional  // Read-write transaction
public class AgentCommandService implements CreateAgentUseCase, UpdateAgentUseCase {
    private final AgentRepository repository;
    private final EventPublisherPort eventPublisher;
    private final AgentCachePort cache;
    
    public Agent createAgent(CreateAgentCommand cmd) {
        // 1. Create domain object
        // 2. Save via repository
        // 3. Publish events
        // 4. Invalidate cache
        // 5. Return domain object
    }
}
```

**Query Side (Read Operations):**
```java
@Service
@Transactional(readOnly = true)  // Read-only transaction
public class AgentQueryService implements FindAgentUseCase {
    private final AgentRepository repository;
    private final AgentCachePort cache;
    
    public Optional<Agent> findByAgentCode(String code) {
        // 1. Check cache first
        // 2. Query repository if cache miss
        // 3. Cache result
        // 4. Return domain object
    }
}
```

---

## 🧪 Testing Guidelines

```java
// Domain Tests - Pure unit tests
@Test
void shouldActivateAgent() {
    Agent agent = Agent.create(...);
    agent.activate();
    assertTrue(agent.isActive());
}

// Application Tests - Mock ports
@ExtendWith(MockitoExtension.class)
class AgentCommandServiceTest {
    @Mock private AgentRepository repository;
    @Mock private EventPublisherPort eventPublisher;
    
    @Test
    void shouldCreateAgent() {
        // Test orchestration logic
    }
}

// Infrastructure Tests - Integration tests
@SpringBootTest
@Transactional
class AgentRepositoryAdapterTest {
    @Autowired private AgentRepositoryAdapter adapter;
    
    @Test
    void shouldSaveAgent() {
        // Test persistence
    }
}
```

---

## 🔍 Quick Decision Tree

**When asked to create a...**

**Entity?**
→ Domain layer, implements `AggregateRoot`, pure Java, business methods

**Value Object?**
→ Domain layer, record, immutable, self-validating

**Repository?**
→ Interface in domain, implementation (Adapter) in infrastructure

**Use Case?**
→ Interface in `application.port.in`, implementation in `application.service`

**DTO?**
→ Application layer, static `from()` method to convert from domain

**Controller?**
→ Infrastructure layer, uses use cases, returns DTOs

**Service?**
→ Application layer, implements use cases, uses repositories (via interfaces)

---

## 📚 Additional Resources

- [Architectural Compliance Rules](docs/development/ARCHITECTURAL_COMPLIANCE_RULES.md)
- [Best Practices](docs/development/best-practices.md)
- [Component Creation Guide](docs/development/component-creation-guide.md)
- [Architecture Diagrams](docs/architecture/code-diagrams/code-diagrams.md)

---

**Remember:** If unsure about a pattern or dependency, check `ArchitectureTest.java` - it enforces all these rules automatically!
