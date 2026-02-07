# AI Digital Signage Microservices Template

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-green)
![Architecture](https://img.shields.io/badge/Architecture-Package%20by%20Component-purple)
![License](https://img.shields.io/badge/License-Proprietary-red)

> **A production-ready Spring Boot microservice template** implementing Package by Component architecture with DDD, Hexagonal Architecture, and CQRS patterns.

---

## 🎯 Purpose

This template provides a **clean, maintainable foundation** for building microservices. It's designed to be:
- **AI-Friendly**: GitHub Copilot can easily generate new components following the established patterns
- **Developer-Friendly**: Clear structure, comprehensive examples, and extensive documentation
- **Production-Ready**: Includes observability, testing, and deployment configurations

---

## 🏗️ Architecture

### Package by Component Pattern

Organize code by **business capability** rather than technical layers:

```
io.jeecloud.aidigitalsignage/
├── agent/                # Agent Business Component (COMPLETE)
│   ├── domain/           # Business rules & entities
│   ├── application/      # Use cases & orchestration (CQRS)
│   │   ├── port/in/      # Input ports (use case interfaces)
│   │   ├── port/out/     # Output ports (repository, event)
│   │   ├── service/      # Command & Query services
│   │   ├── dto/          # Data Transfer Objects
│   │   └── mapper/       # DTO mappings
│   └── infrastructure/   # REST, DB, Kafka adapters
│
├── user/                 # User Business Component (COMPLETE)
│   ├── domain/           # User aggregate, value objects
│   ├── application/      # Use cases & orchestration (CQRS)
│   │   ├── port/in/      # Input ports (use case interfaces)
│   │   ├── port/out/     # Output ports (repository, event)
│   │   ├── service/      # Command & Query services
│   │   ├── dto/          # Data Transfer Objects
│   │   └── mapper/       # DTO mappings
│   └── infrastructure/   # REST, DB, Kafka adapters
│       └── persistence/  # UserAgentEntity (junction table)
│
├── common/               # Shared Kernel (from common module)
│   ├── domain/base/      # Entity, ValueObject, AggregateRoot
│   ├── application/      # Command, Query, Ports
│   └── infrastructure/   # Config, REST common, Messaging
```

### Key Principles

1. **Component Isolation**: `agent/`, `user/` are independent components
2. **Shared Kernel**: Common abstractions from `common` module (`io.jeecloud.aidigitalsignage.common`)
3. **Dependency Rule**: Domain → Application → Infrastructure (all depend on Common)
4. **Rich Domain Model**: Business logic lives in domain entities, not services
5. **CQRS**: Separate command (write) and query (read) operations in application layer

📖 **Deep Dive**: [Architecture Code Diagrams](docs/architecture/code-diagrams/code-diagrams.md)  
🤖 **AI Instructions**: [.github/copilot-instructions.md](.github/copilot-instructions.md)  
🛠️ **Tech Stack**: [Architecture Code Diagrams - Technology Stack](docs/architecture/code-diagrams/code-diagrams.md#3-technology-stack)  
📚 **API Docs**: [Swagger/OpenAPI Guide](docs/api/api-documentation.md)

---

## 📦 What's Included

### Example Components

#### Agent Component (Complete CRUD)
A complete business capability demonstrating:
- **Domain Layer**: Aggregate roots, value objects, domain events
- **Application Layer**: Use cases, DTOs, mappers, CQRS pattern
- **Infrastructure Layer**: REST controllers, JPA repositories, Kafka consumers
- **Key Features**: Full CRUD operations, status management, event publishing

#### User Component (Component Relationships)
Demonstrates inter-component patterns:
- **Many-to-Many Relationships**: User ↔ Agent via junction table
- **Loose Coupling**: References Agent via `AgentCode` value object (not aggregate)
- **Junction Table Pattern**: `UserAgentEntity` for relationship persistence
- **Repository Pattern**: Complex queries with association management
- **Key Pattern**: Shows how components interact without direct dependencies

**See**: [Architecture Code Diagrams - Component Relationships](docs/architecture/code-diagrams/code-diagrams.md#6-component-relationships) for detailed patterns

### Common Library (from common module)
- **Base Interfaces**: `Entity`, `ValueObject`, `AggregateRoot`, `DomainEvent`
- **CQRS Markers**: `Command`, `Query`
- **Output Ports**: `EventPublisherPort`, `CachePort`
- **REST Common**: `ApiResponse`, `PageRequest`, `PageResponse`
- **Configurations**: Kafka, Redis, Web, Observability

**Package**: `io.jeecloud.aidigitalsignage.common.*` (provided by `common` Maven dependency)

### Infrastructure & Deployment
- **Database**: H2 (dev), Aurora PostgreSQL (UAT/prod) with Flyway migrations
- **Messaging**: Confluent Kafka (SCC+) for event-driven communication
- **Caching**: AWS ElastiCache Redis for distributed caching
- **Observability**: Actuator, Micrometer, Prometheus, Dynatrace (prod)
- **Testing**: Unit, integration, architecture tests with ArchUnit
- **Deployment**: Docker multi-stage builds, AWS EKS, Terraform IaC
- **Security**: AWS KMS encryption, Secrets Manager, TLS 1.3+

---

## 🚀 Quick Start

### Prerequisites
- Java 21+ (LTS)
- Maven 3.8+
- Docker Desktop (for Redis/Kafka)
- Redis Server (for local profile)

### Local Development (Individual Developer Machine)

1. **Start local Redis:**
```bash
# Option 1: Docker
docker run -d -p 6379:6379 --name redis redis:7-alpine

# Option 2: Native Redis installation
redis-server
```

2. **Run the application (local profile):**
```bash
export SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run
```

3. **Access:**
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:testdb`)
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/prometheus

**Redis Configuration (Local)**:
- Host: `127.0.0.1`
- Port: `6379`
- Database: `0`

### Shared Development Environment

1. **Start infrastructure:**
```bash
docker-compose up -d
```

This starts:
- Kafka + Zookeeper
- Redis
- Kafka UI (http://localhost:8090)

2. **Run the application (dev profile):**
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### UAT/Production Deployment

See [Environment Configuration](#-environment-configuration) section for profile-specific setup.

---

## 🌍 Environment Configuration

The template supports four environments with different configurations:

### Local (`local` profile)
- **Database**: H2 in-memory
- **Kafka**: Optional (local/Docker)
- **Redis**: **Local Redis at 127.0.0.1:6379** (required)
- **Monitoring**: Full debug logging, H2 console enabled
- **Availability**: Single developer machine
- **Use Case**: Individual developer machines with local Redis instance

```bash
# Start local Redis
docker run -d -p 6379:6379 redis:7-alpine

# Run application
export SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run
```

### Development (`dev` profile)
- **Database**: H2 in-memory
- **Kafka**: Local (localhost:9092)
- **Redis**: Local (localhost:6379)
- **Monitoring**: Full debug logging, H2 console enabled
- **Availability**: Single zone
- **Use Case**: Shared development environment for team collaboration

```bash
# Set profile
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### UAT (`uat` profile)
- **Database**: AWS RDS Aurora PostgreSQL (Multi-AZ)
- **Kafka**: Confluent Kafka on AWS (SCC+) with SASL_SSL
- **Redis**: AWS ElastiCache Redis with TLS
- **Monitoring**: AWS CloudWatch logs to S3
- **Availability**: Multi-AZ (2 zones)
- **Environments**: UATBAU, UATPROJ, PREPROD
- **Use Case**: User acceptance testing and pre-production validation

```bash
# Required environment variables
export SPRING_PROFILES_ACTIVE=uat
export DB_USERNAME=<username>
export DB_PASSWORD=<password>
export KAFKA_API_KEY=<confluent-key>
export KAFKA_API_SECRET=<confluent-secret>
export REDIS_PASSWORD=<redis-password>

mvn spring-boot:run
```

### Production (`prod` profile)
- **Database**: AWS RDS Aurora PostgreSQL (Multi-AZ with read replicas)
- **Kafka**: Confluent Kafka on AWS (SCC+) with SASL_SSL, LZ4 compression
- **Redis**: AWS ElastiCache Redis Cluster with TLS
- **Monitoring**: Dynatrace APM (SCC+)
- **Security**: AWS KMS encryption, Secrets Manager, TLS 1.3
- **Availability**: Multi-AZ (2 zones) + Disaster Recovery
- **Performance**: HikariCP connection pooling (50 max), Redis clustering
- **Use Case**: Production workloads

```bash
# Secrets fetched from AWS Secrets Manager
export SPRING_PROFILES_ACTIVE=prod
export AWS_REGION=eu-central-1
export KMS_KEY_ID=<kms-key-id>

mvn spring-boot:run
```

**Configuration Files:**
- [application.yml](src/main/resources/application.yml) - Base configuration
- [application-local.yml](src/main/resources/application-local.yml) - Local (Redis at 127.0.0.1:6379)
- [application-dev.yml](src/main/resources/application-dev.yml) - Development
- [application-uat.yml](src/main/resources/application-uat.yml) - UAT
- [application-prod.yml](src/main/resources/application-prod.yml) - Production

---

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Architecture rules
mvn test -Dtest=ArchitectureTest

# Coverage report (target/site/jacoco/index.html)
mvn clean test jacoco:report
```

---

## 📡 API Examples

### Create Agent
```bash
curl -X POST http://localhost:8080/api/v1/agents \
  -H "Content-Type: application/json" \
  -d '{
    "agentCode": "AGT100",
    "name": "John Doe",
    "branchCode": "BR0001",
    "channel": "DIRECT"
  }'
```

### Get Agent
```bash
curl http://localhost:8080/api/v1/agents/{id}
curl http://localhost:8080/api/v1/agents/code/AGT100
```

### Update Agent
```bash
curl -X PUT http://localhost:8080/api/v1/agents/{id} \
  -H "Content-Type: application/json" \
  -d '{"name": "Jane Doe", "channel": "BROKER"}'
```

---

## 📂 Project Structure

```
src/main/java/com/allianz/sat/
├── agent/                          # Agent Component (CRUD example)
│   ├── domain/
│   │   ├── Agent.java             # Aggregate Root
│   │   ├── AgentCode.java         # Value Object (shareable)
│   │   ├── AgentRepository.java   # Repository Port
│   │   ├── event/                 # Domain Events
│   │   └── exception/             # Domain Exceptions
│   ├── application/
│   │   ├── port/in/               # Use Case Interfaces
│   │   ├── service/               # Command & Query Services
│   │   ├── dto/                   # Data Transfer Objects
│   │   └── mapper/                # Domain ↔ DTO Mappers
│   └── infrastructure/
│       ├── rest/                  # REST Controllers
│       ├── persistence/           # JPA Entities & Repositories
│       └── messaging/             # Event Consumers/Publishers
│
├── user/                           # User Component (relationships example)
│   ├── domain/
│   │   ├── User.java              # Aggregate Root
│   │   ├── UserId.java            # Value Object
│   │   └── UserRepository.java    # Repository Port
│   ├── application/
│   │   ├── service/               # Command & Query Services
│   │   └── dto/                   # Data Transfer Objects
│   └── infrastructure/
│       └── persistence/
│           ├── UserEntity.java
│           ├── UserAgentEntity.java  # Junction table for User-Agent
│           └── UserRepositoryAdapter.java
│
└── SatMicroservicesTemplateApplication.java

Note: Common components (Entity, ValueObject, Command, Query, etc.) 
are imported from the common Maven dependency:
  io.jeecloud.aidigitalsignage.common.domain.base.*
  io.jeecloud.aidigitalsignage.common.application.common.*
  io.jeecloud.aidigitalsignage.common.infrastructure.*

src/main/resources/
├── application.yml
├── application-local.yml
├── application-dev.yml
├── application-uat.yml
├── application-prod.yml
└── db/migration/
    └── V1__initial_schema.sql

src/test/java/                      # Tests mirror main structure
├── agent/
├── user/
└── architecture/
    └── ArchitectureTest.java       # ArchUnit rules validation
---

## 🤖 AI-Assisted Development

This template is optimized for GitHub Copilot:

### Generate a New Component

Ask Copilot: *"Create a new policy component with CRUD operations following the agent pattern"*

Copilot will generate:
- **Domain layer** (Policy aggregate, PolicyId, PolicyRepository)
- **Application layer** (use cases with port/in and port/out, command/query services, DTOs, mappers)
- **Infrastructure layer** (REST controller, JPA entities, repository adapter)
- Following all architectural rules automatically

**Note**: Both Agent and User components provide complete implementation examples with application layer structure.

### Architectural Rules Enforced

The `.github/copilot-instructions.md` file ensures Copilot generates code that:
✅ Follows Package by Component structure  
✅ Implements DDD patterns correctly  
✅ Maintains proper dependency direction  
✅ Uses shared kernel appropriately  
✅ Includes validation and error handling  

---

## 📚 Documentation

- **[Architecture Code Diagrams](docs/architecture/code-diagrams/code-diagrams.md)** - Complete architecture diagrams and patterns
- **[AI Digital Signage Menu Structure](../legacy_system/allianz-sat-menu-structure.md)** - Business context
- **[AI Instructions](.github/copilot-instructions.md)** - Complete guide for AI code generation

---

## 🛠️ Technology Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.4.2 |
| **Database** | H2 (dev), PostgreSQL (prod) |
| **Messaging** | Apache Kafka |
| **Caching** | Redis |
| **Testing** | JUnit 5, Mockito, AssertJ, Testcontainers, ArchUnit |
| **Build** | Maven 3.8+ |
| **Observability** | Actuator, Micrometer, Prometheus |
| **Migration** | Flyway |
| **Deployment** | Docker, Kubernetes, Terraform |

---

## 🚢 Deployment

### Docker
```bash
docker build -t sat-microservice:latest .
docker-compose up
```

### Kubernetes
```bash
kubectl apply -f infrastructure/k8s/
kubectl get pods -n allianz-sat
```

### Terraform (AWS)
```bash
cd infrastructure/terraform
terraform init
terraform apply
```

Creates: EKS cluster, MSK (Kafka), ElastiCache (Redis), VPC

---

## 📊 Monitoring & Health

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Application health status |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-formatted metrics |
| `/actuator/info` | Application information |

---

## 🎓 Learning Resources

### Architecture Patterns
- [Explicit Architecture](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)
- [Package by Component vs Package by Layer](https://phauer.com/2020/package-by-feature/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)

### Template Documentation
- **Getting Started**: [README](README.md) → [component-creation-guide.md](docs/development/component-creation-guide.md)
- **Architecture**: [Architecture Code Diagrams](docs/architecture/code-diagrams/code-diagrams.md)
- **Testing**: [testing-strategies.md](docs/testing/testing-strategies.md)
- **Troubleshooting**: [troubleshooting.md](docs/development/troubleshooting.md)

### Reference Implementations
- Study the `agent/` component for complete CRUD operations
- Study the `user/` component for relationship patterns
- Review `UserAgentEntity.java` for junction table implementation
- Check [ArchitectureTest.java](src/test/java/com/allianz/sat/architecture/ArchitectureTest.java) for enforced rules

---

## 🤝 Contributing

### Before Creating a Component

1. Understand the business capability
2. Define aggregates and bounded context
3. Sketch domain events
4. Follow naming conventions

### Code Quality Checks

```bash
mvn clean verify                  # All tests
mvn test -Dtest=ArchitectureTest  # Architecture rules
mvn jacoco:report                 # Coverage report
```

### Commit Message Format
```
<type>(<scope>): <subject>

feat(agent): add agent activation use case
fix(shared): resolve cache port TTL issue
docs(readme): update API examples
```

---

## 📝 License

Proprietary - Allianz Technology SE

---

## 🆘 Support

- **Issues**: Report in project repository
- **Questions**: Contact SAT team
- **Documentation**: Check `docs/` directory

---

**Happy Coding! 🚀**

*This template is designed to accelerate microservice development while maintaining architectural integrity. Use it as a foundation, adapt to your needs, but preserve the core principles.*
