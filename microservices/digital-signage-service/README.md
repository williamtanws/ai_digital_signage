# Digital Signage Service

Edge AI–powered digital signage backend service for audience analytics and reporting dashboard.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [API Endpoint](#api-endpoint)
  - [Get Dashboard Overview](#get-dashboard-overview)
- [Project Structure](#project-structure)
- [Running the Service](#running-the-service)
  - [Prerequisites](#prerequisites)
  - [Quick Start with SQLite (Recommended)](#quick-start-with-sqlite-recommended)
  - [Manual Build and Run](#manual-build-and-run)
  - [Access Points](#access-points)
  - [Test the API](#test-the-api)
- [Configuration](#configuration)
  - [Server Port](#server-port)
  - [Database Configuration](#database-configuration)
  - [CORS Configuration](#cors-configuration)
- [Mock Data (Database Implementation)](#mock-data-database-implementation)
- [Development Notes](#development-notes)
  - [Database Integration](#database-integration)
  - [No Authentication](#no-authentication)
  - [CORS Enabled](#cors-enabled)
  - [Logging](#logging)
- [Frontend Integration](#frontend-integration)
- [Future Enhancements](#future-enhancements)
- [License](#license)
- [Contact](#contact)

## Overview

This Spring Boot service provides a single REST API endpoint that returns comprehensive dashboard data for visualizing audience analytics from edge AI devices.

**Purpose**: Academic evaluation and SME demonstration  
**Data Source**: Mock data only (no database or persistence)  
**Architecture**: Simple Spring Boot REST service

## Technology Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 3.5.10
- **Build Tool**: Maven
- **Dependencies**: Spring Web, Lombok

## API Endpoint

### Get Dashboard Overview

```
GET /api/dashboard/overview
```

Returns complete dashboard data in a single response.

**Response Structure**:
```json
{
  "totalAudience": 1247,
  "totalViews": 3856,
  "totalAds": 12,
  "avgViewSeconds": 24.5,
  "ageDistribution": {
    "children": 150,
    "teenagers": 225,
    "youngAdults": 437,
    "midAged": 312,
    "seniors": 123
  },
  "genderDistribution": {
    "male": 648,
    "female": 599
  },
  "emotionDistribution": {
    "neutral": 561,
    "serious": 312,
    "happy": 274,
    "surprised": 100
  },
  "adsPerformance": [
    {
      "adName": "Summer Sale 2026",
      "totalViewers": 485
    },
    ...
  ],
  "adsAttention": [
    {
      "adName": "Summer Sale 2026",
      "lookYes": 388,
      "lookNo": 97
    },
    ...
  ]
}
```

## Project Structure

```
src/main/java/io/jeecloud/aidigitalsignage/digitalsignage/
├── DigitalSignageServiceApplication.java    # Main application class
├── config/
│   └── CorsConfig.java                      # CORS configuration
├── controller/
│   └── DashboardController.java             # REST endpoint
├── service/
│   └── DashboardService.java                # Business logic and mock data
└── dto/
    ├── DashboardOverviewResponse.java       # Main response DTO
    ├── AgeDistributionDto.java              # Age demographics
    ├── GenderDistributionDto.java           # Gender demographics
    ├── EmotionDistributionDto.java          # Emotion analysis
    ├── AdsPerformanceDto.java               # Ad viewer counts
    └── AdsAttentionDto.java                 # Ad attention metrics
```

## Running the Service

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Docker Desktop (for SQLite browser container)

### Quick Start with SQLite (Recommended)

Use the provided startup script that automatically starts the SQLite browser and the service:

**PowerShell:**
```powershell
cd microservices/digital-signage-service
.\start-with-sqlite.ps1
```

**Command Prompt:**
```cmd
cd microservices\digital-signage-service
start-with-sqlite.bat
```

This will:
1. Set Java 21 environment
2. Start SQLite browser container on port 3000
3. Create the data directory
4. Start the Spring Boot service on port 8080

**To stop all services:**
```powershell
.\stop-services.ps1
```

### Manual Build and Run

```bash
# Navigate to the project directory
cd microservices/digital-signage-service

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The service will start on **http://localhost:8080**

### Access Points

- **Backend API**: http://localhost:8080/api/dashboard/overview
- **Frontend Dashboard**: http://localhost:5175 (Vue.js + Vite)
- **SQLite Browser**: http://localhost:3000 (Docker container)
- **Database File**: `./data/digital-signage.db`

### Test the API

```bash
# Using curl
curl http://localhost:8080/api/dashboard/overview

# Using browser
# Navigate to: http://localhost:8080/api/dashboard/overview
```

## Configuration

### Server Port
Default: `8080`

To change, edit `src/main/resources/application.yml`:
```yaml
server:
  port: 8080
```

### Database Configuration
The service uses **SQLite** for persistent storage:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${SQLITE_DB_PATH:./data/digital-signage.db}
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
```

To use a different database location:
```bash
export SQLITE_DB_PATH=/custom/path/database.db
mvn spring-boot:run
```

### CORS Configuration
Configured to allow requests from common frontend development servers:
- http://localhost:3000
- http://localhost:5173 (Vite default)
- http://localhost:5175 (Current frontend)
- http://localhost:8081

## Mock Data (Database Implementation)

The service uses **Flyway migrations** to populate the SQLite database with realistic mock data:

### KPIs
- **Total Audience**: 1,247 unique visitors
- **Total Views**: 3,856 viewing sessions
- **Total Ads**: 12 different advertisements
- **Avg View Seconds**: 24.5 seconds

### Demographics
- **Age Groups**: Children, Teenagers, Young Adults, Mid-Aged, Seniors
- **Gender**: Male, Female

### Emotions
- **Categories**: Neutral, Serious, Happy, Surprised

### Advertisement Analytics
- **Performance**: Viewer counts per ad
- **Attention**: Look yes/no counts per ad

## Development Notes

### Database Integration
The service includes **Spring Data JPA** with **SQLite** support:
- **Database**: SQLite 3.47.2.0
- **ORM**: Hibernate with SQLite dialect
- **Schema Management**: Flyway migrations (version-controlled SQL scripts)
- **Location**: `./data/digital-signage.db` (created on first run)
- **Browser Tool**: SQLite browser available at http://localhost:3000 (Docker)
- **Mock Data**: Populated via Flyway migration scripts (`V1__Create_tables.sql`, `V2__Insert_mock_data.sql`)

**Database Schema:**
- `metrics_kpi` - Dashboard KPI metrics
- `age_distribution` - Audience age demographics
- `gender_distribution` - Audience gender demographics  
- `emotion_distribution` - Emotion analysis data
- `advertisement` - Ad performance and attention metrics

### No Authentication
For prototype purposes, the API is open without authentication.

### CORS Enabled
The service allows cross-origin requests from localhost for frontend integration.

### Logging
- Application logs are output to console
- Log level: INFO (root), DEBUG (application packages)

## Frontend Integration

The frontend dashboard should:
1. Make a single GET request to `/api/dashboard/overview`
2. Parse the JSON response
3. Display KPIs and render charts from the data

Example using Axios (JavaScript):
```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

async function fetchDashboardData() {
  const response = await axios.get(`${API_BASE_URL}/api/dashboard/overview`);
  return response.data;
}
```

## Future Enhancements

For production deployment, consider adding:
- ✅ **Database persistence** (SQLite integrated, ready for PostgreSQL migration)
- Implement JPA entities and repositories to replace mock data
- Spring Security for authentication
- Redis caching for performance
- Real-time data from edge AI devices via Kafka/MQTT
- Historical data analysis and trends
- Alerting and notifications
- Multi-tenant support

## License

This is an academic prototype for evaluation purposes.

## Contact

For questions or issues, please refer to the main project documentation.
