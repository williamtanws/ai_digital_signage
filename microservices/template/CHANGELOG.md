# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-11

### Added - Initial Release

#### Domain Layer
- **Shared Base Interfaces**
  - `Entity<ID>` - Base interface for entities with identity
  - `ValueObject` - Marker interface for value objects
  - `AggregateRoot<ID>` - Base interface for aggregate roots with domain events
  
- **Agent Aggregate**
  - `Agent` - Aggregate root with business logic
  - `AgentId` - UUID-based value object for agent identification
  - `AgentCode` - Alphanumeric business code value object (6-10 chars)
  - `Channel` - Enum for distribution channels (DIRECT, BROKER, BANCASSURANCE, ONLINE, TELEMARKETING)
  - `AgentRepository` - Port interface for persistence
  - `AgentDomainService` - Domain service for cross-aggregate business logic

- **Domain Events**
  - `DomainEvent` - Base interface for all domain events
  - `AgentCreatedEvent` - Published when agent is created
  - `AgentUpdatedEvent` - Published when agent is updated

- **Domain Exceptions**
  - `DomainException` - Base exception for business rule violations
  - `AgentNotFoundException` - Specific exception for agent not found scenarios

#### Application Layer
- **CQRS Markers**
  - `Command` - Marker interface for write operations
  - `Query<T>` - Marker interface for read operations
  - `ApplicationEvent` - Base class for application-level events

- **Input Ports (Use Cases)**
  - `CreateAgentUseCase` - Use case for agent creation with `CreateAgentCommand`
  - `UpdateAgentUseCase` - Use case for agent updates with `UpdateAgentCommand`
  - `DeleteAgentUseCase` - Use case for agent deletion
  - `FindAgentUseCase` - Use case for agent queries
  - `ManageAgentStatusUseCase` - Use case for activate/deactivate operations

- **Output Ports**
  - `EventPublisherPort` - Port for publishing domain events
  - `CachePort` - Port for cache operations

- **Application Services**
  - `AgentCommandService` - CQRS command side (Create, Update, Delete)
  - `AgentQueryService` - CQRS query side with Redis caching
  - `AgentStatusService` - Dedicated service for status management

- **DTOs & Mappers**
  - `AgentRequest` - Request DTO with Jakarta validation
  - `AgentResponse` - Response DTO
  - `AgentMapper` - MapStruct mapper for DTO conversions

#### Infrastructure Layer
- **REST API (API v1)**
  - `AgentController` - RESTful API at `/api/v1/agents`
    - POST `/` - Create agent
    - GET `/{id}` - Get by ID
    - GET `/code/{code}` - Get by code
    - GET `/` - List with filters (branchCode, channel, active)
    - PUT `/{id}` - Update agent
    - DELETE `/{id}` - Delete agent
  - `AgentExceptionHandler` - Global exception handling with ProblemDetail (RFC 7807)
  - `ApiResponse<T>` - Standard response wrapper
  - `PageRequest` / `PageResponse<T>` - Pagination support

- **Persistence (JPA)**
  - `AgentEntity` - JPA entity with indexes
  - `AgentJpaRepository` - Spring Data JPA repository
  - `AgentRepositoryAdapter` - Adapter implementing domain `AgentRepository`

- **Messaging (Kafka)**
  - `KafkaEventPublisher` - Publishes events to "agent-events" topic
  - `KafkaEventConsumer` - Consumes and processes events
  - `KafkaConfig` - Topic configuration (3 partitions, replication factor 1)

- **Caching (Redis)**
  - `RedisCacheAdapter` - Implements `CachePort` with JSON serialization
  - `RedisConfig` - RedisTemplate and ObjectMapper configuration

- **Configuration**
  - `ObservabilityConfig` - Micrometer metrics with common tags
  - `WebConfig` - CORS configuration
  - `ApplicationProperties` - Type-safe configuration properties

- **Health Checks**
  - `AgentServiceHealthIndicator` - Custom health indicator for business logic

#### Testing
- **Domain Tests**
  - `AgentTest` - Aggregate root behavior tests
  - `AgentCodeTest` - Value object validation tests

- **Application Tests**
  - `AgentCommandServiceTest` - Command service tests with mocks

- **Integration Tests**
  - `AgentControllerTest` - REST API integration tests

- **Architecture Tests**
  - `ArchitectureTest` - ArchUnit tests enforcing layer dependencies and naming conventions

#### Database
- **Flyway Migrations**
  - `V1__Create_Agent_Table.sql` - Schema creation with indexes
  - `V2__Insert_Sample_Agents.sql` - 10 sample agents (AGT001-AGT010)

#### Infrastructure as Code
- **Docker**
  - Multi-stage `Dockerfile` with Maven builder and JRE runtime
  - `docker-compose.yml` - Full stack (app, Zookeeper, Kafka, Redis, Kafka UI)

- **Kubernetes**
  - `namespace.yml` - allianz-sat namespace
  - `deployment.yml` - Deployment, Service, HorizontalPodAutoscaler (3-10 replicas)

- **Terraform (AWS)**
  - VPC with public/private subnets
  - EKS cluster (Kubernetes 1.28)
  - MSK (Managed Kafka) - 3 brokers
  - ElastiCache (Redis)
  - Security groups and IAM roles

#### Documentation
- `README.md` - Quick start and API reference
- `docs/development/cheatsheet.md` - Quick reference for common tasks
- `docs/architecture/code-diagrams/code-diagrams.md` - Comprehensive architecture guide
- `docs/development/best-practices.md` - Patterns and principles applied
- `docs/development/development-guide.md` - Development workflow and standards
- `docs/deployment/deployment-guide.md` - Deployment procedures
- `docs/README.md` - Documentation navigation hub
- `CHANGELOG.md` - Version history (this file)

### Technical Specifications
- **Java**: 17
- **Spring Boot**: 3.2.1
- **Spring Cloud**: 2023.0.0
- **Maven**: 3.8+
- **Kafka**: 3.5.1
- **Redis**: 7.0
- **PostgreSQL**: 15+ (production)
- **H2**: 2.2.224 (development)

### Architecture Characteristics
- **Ports & Adapters**: Clean separation of concerns
- **CQRS**: Separate read and write models
- **Event-Driven**: Asynchronous domain events via Kafka
- **Caching Strategy**: Cache-aside with Redis (3600s TTL)
- **API Versioning**: URL-based versioning (/api/v1)
- **Database Migrations**: Version-controlled with Flyway
- **Observability**: Actuator, Micrometer, Prometheus metrics
- **Containerization**: Docker multi-stage builds
- **Orchestration**: Kubernetes with HPA
- **Cloud-Ready**: Terraform AWS infrastructure

### Design Patterns Applied
- Aggregate Root
- Value Object
- Repository
- Factory Method
- Domain Event
- Port and Adapter
- Command Query Responsibility Segregation (CQRS)
- Event Sourcing (partial)
- Dependency Inversion
- Single Responsibility Principle

[1.0.0]: https://github.com/your-repo/sat-microservices-guide-template/releases/tag/v1.0.0
