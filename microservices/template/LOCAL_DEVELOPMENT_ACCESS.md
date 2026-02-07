# Local Development Environment Access Guide

This document provides URLs and credentials for accessing various services in the local development environment.

## Application Services

### Main Application
- **URL:** http://localhost:8080
- **Profile:** local
- **Context Path:** /

### Swagger API Documentation
- **URL:** http://localhost:8080/swagger-ui.html
- **Alternative URL:** http://localhost:8080/swagger-ui/index.html
- **Username:** `user`
- **Password:** `user`

### Actuator Endpoints
- **Base URL:** http://localhost:8080/actuator
- **Health Check:** http://localhost:8080/actuator/health

## Database Services

### H2 Database Console
- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** _(leave empty)_
- **Driver Class:** `org.h2.Driver`

> **Note:** H2 is an in-memory database. All data will be lost when the application stops.

## Message Queue Services

### Kafka Broker
- **Bootstrap Servers:** localhost:9092
- **Internal Port:** localhost:9093
- **Protocol:** PLAINTEXT
- **Consumer Group:** template-group

#### Kafka Topics
- **User Events:** `local.agent-events`
- **User Active Status:** `user-active-status` (consumed by Agent component for one-way sync)

### Kafka UI (Management Console)
- **URL:** http://localhost:8090
- **Cluster Name:** local
- **Purpose:** Monitor and manage Kafka topics, messages, and consumers

### Zookeeper
- **Host:** localhost:2181
- **Purpose:** Kafka coordination service

## Cache Services

### Redis
- **Host:** 127.0.0.1
- **Port:** 6379
- **Password:** _(none)_
- **Database:** 0
- **Timeout:** 60000ms

#### Redis Key Prefixes
- **Agent Cache:** `local:agent:`
- **TTL:** 1800 seconds (30 minutes)

### Redis Commander (Management UI)
- **URL:** http://localhost:8081
- **Purpose:** Browse and manage Redis keys and data

## Docker Services

All services are managed via Docker Compose:

```bash
# Start all services
cd template
docker-compose -f docker-compose-local.yml up -d

# Stop all services
docker-compose -f docker-compose-local.yml down

# View logs
docker-compose -f docker-compose-local.yml logs -f

# Check service status
docker-compose -f docker-compose-local.yml ps
```

## Starting the Application

```bash
# From project root
cd template

# Start with local profile (skipping tests)
mvn spring-boot:run '-Dspring-boot.run.profiles=local' '-Dmaven.test.skip=true'
```

## Log Files

- **Location:** `template/logs/ai-ds-service-local.log`
- **Log Level:** DEBUG for application code, INFO for frameworks

## Quick Access Summary

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | N/A |
| Swagger UI | http://localhost:8080/swagger-ui.html | N/A |
| H2 Console | http://localhost:8080/h2-console | sa / _(empty)_ |
| Redis Commander | http://localhost:8081 | N/A |
| Kafka UI | http://localhost:8090 | N/A |
| Actuator Health | http://localhost:8080/actuator/health | N/A |

## Common Issues

### Port Already in Use
If port 8080 is already in use, stop the process:
```powershell
$process = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -First 1
if ($process) { Stop-Process -Id $process -Force }
```

### Docker Services Not Running
Ensure Docker Desktop is running and start the services:
```bash
docker-compose -f template/docker-compose-local.yml up -d
```

### Cannot Connect to Kafka/Redis
Check that Docker containers are running:
```bash
docker ps
```

You should see containers: `sat-kafka`, `sat-redis`, `sat-zookeeper`
