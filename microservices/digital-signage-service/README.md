# Digital Signage Service

Edge AI–powered digital signage backend service for audience analytics and reporting dashboard.

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

### Build and Run

```bash
# Navigate to the project directory
cd microservices/digital-signage-service

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The service will start on **http://localhost:8080**

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

### CORS Configuration
Configured to allow requests from common frontend development servers:
- http://localhost:3000
- http://localhost:5173 (Vite default)
- http://localhost:8081

## Mock Data

The service generates realistic mock data representing:

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

### No Database Required
This is a stateless service with hardcoded mock data. No database configuration or JPA entities are needed.

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
- Real database persistence (PostgreSQL, MongoDB)
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
