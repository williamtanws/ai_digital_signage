# Getting Started - Digital Signage Dashboard

Complete guide to running the Edge AI Digital Signage Analytics Dashboard.

## Project Overview

This project demonstrates audience analytics for digital signage systems using:
- **Backend**: Spring Boot REST API (Mock Data)
- **Frontend**: Vue 3 Dashboard (Data Visualization)

## System Requirements

### Backend Requirements
- **Java**: 21 or higher (LTS recommended)
- **Maven**: 3.6+ (or use embedded Maven wrapper)
- **Port**: 8080 (configurable)

### Frontend Requirements
- **Node.js**: 18+ or 20+ (LTS recommended)
- **npm**: Comes with Node.js
- **Port**: 5173 (default Vite port)

## Quick Start Guide

### Step 1: Start the Backend Service

```bash
# Navigate to backend directory
cd microservices/digital-signage-service

# Build and run the Spring Boot application
mvn spring-boot:run
```

**Expected Output**:
```
...
Started DigitalSignageServiceApplication in X.XXX seconds
```

The backend API will be available at: **http://localhost:8080**

**Verify backend is running**:
```bash
# Using curl
curl http://localhost:8080/api/dashboard/overview

# Or open in browser:
# http://localhost:8080/api/dashboard/overview
```

You should see JSON data with dashboard metrics.

---

### Step 2: Start the Frontend Dashboard

**Open a NEW terminal window** (keep backend running in the first terminal)

```bash
# Navigate to frontend directory
cd microservices/digital-signage-dashboard

# Install dependencies (first time only)
npm install

# Start the development server
npm run dev
```

**Expected Output**:
```
  VITE v6.x.x  ready in XXX ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

The dashboard will be available at: **http://localhost:5173**

---

### Step 3: Access the Dashboard

Open your browser and navigate to:

**http://localhost:5173**

You should see:
- ✅ 4 KPI cards at the top
- ✅ 6 analytics charts below
- ✅ Data populated from the backend API

## Project Structure

```
microservices/
├── digital-signage-service/          # Backend (Spring Boot)
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── io/jeecloud/aidigitalsignage/digitalsignage/
│   │       │       ├── DigitalSignageServiceApplication.java
│   │       │       ├── config/
│   │       │       │   └── CorsConfig.java
│   │       │       ├── controller/
│   │       │       │   └── DashboardController.java
│   │       │       ├── service/
│   │       │       │   └── DashboardService.java
│   │       │       └── dto/
│   │       │           ├── DashboardOverviewResponse.java
│   │       │           ├── AgeDistributionDto.java
│   │       │           ├── GenderDistributionDto.java
│   │       │           ├── EmotionDistributionDto.java
│   │       │           ├── AdsPerformanceDto.java
│   │       │           └── AdsAttentionDto.java
│   │       └── resources/
│   │           └── application.yml
│   ├── pom.xml
│   └── README.md
│
└── digital-signage-dashboard/         # Frontend (Vue 3)
    ├── src/
    │   ├── components/
    │   │   ├── KpiCard.vue
    │   │   ├── BarChart.vue
    │   │   └── PieChart.vue
    │   ├── services/
    │   │   └── api.js
    │   ├── App.vue
    │   ├── main.js
    │   └── style.css
    ├── index.html
    ├── vite.config.js
    ├── package.json
    └── README.md
```

## API Endpoint Documentation

### GET /api/dashboard/overview

Returns complete dashboard data in a single response.

**URL**: `http://localhost:8080/api/dashboard/overview`

**Method**: `GET`

**Response**:
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
    {
      "adName": "New Product Launch",
      "totalViewers": 432
    }
    // ... 10 more ads
  ],
  "adsAttention": [
    {
      "adName": "Summer Sale 2026",
      "lookYes": 388,
      "lookNo": 97
    },
    {
      "adName": "New Product Launch",
      "lookYes": 346,
      "lookNo": 86
    }
    // ... 10 more ads
  ]
}
```

## Dashboard Features

### KPI Cards
1. **Total Audience**: 1,247 unique visitors
2. **Total Views**: 3,856 content impressions
3. **Total Ads**: 12 advertisements
4. **Average View Time**: 24.5 seconds

### Analytics Charts
1. **Age Distribution** (Bar Chart)
   - Children (0-12)
   - Teenagers (13-19)
   - Young Adults (20-35)
   - Mid-Aged (36-55)
   - Seniors (56+)

2. **Advertisement Performance** (Horizontal Bar Chart)
   - Shows total viewers per advertisement
   - Sorted by performance

3. **Gender Distribution** (Pie Chart)
   - Male vs Female breakdown
   - Percentage display

4. **Emotion Distribution** (Bar Chart)
   - Neutral, Serious, Happy, Surprised
   - Based on facial expression detection

5. **Advertisement Attention** (Horizontal Bar Chart)
   - Look vs No-Look per advertisement
   - Engagement metrics

6. **Overall Attention Rate** (Pie Chart)
   - Aggregated look vs no-look ratio
   - Overall engagement summary

## Stopping the Services

### Stop Frontend
In the frontend terminal, press: `Ctrl + C`

### Stop Backend
In the backend terminal, press: `Ctrl + C`

## Building for Production

### Backend

```bash
cd microservices/digital-signage-service

# Build JAR file
mvn clean package

# Run the JAR
java -jar target/digital-signage-service-1.0.0-SNAPSHOT.jar
```

### Frontend

```bash
cd microservices/digital-signage-dashboard

# Build optimized production files
npm run build

# The output will be in the dist/ folder
```

To serve the production build:
```bash
npm run preview
```

## Troubleshooting

### Backend Issues

#### Port 8080 Already in Use
```bash
# Check what's using port 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Mac/Linux

# Change port in application.yml:
server:
  port: 8081  # Use different port
```

#### Java Version Issues
```bash
# Check Java version
java -version

# Should show Java 21 or higher
# If not, install Java 21 from:
# - OpenJDK: https://adoptium.net/
# - Oracle JDK: https://www.oracle.com/java/technologies/downloads/
```

#### Maven Build Fails
```bash
# Clean and rebuild
mvn clean install

# Skip tests if needed
mvn clean install -DskipTests
```

---

### Frontend Issues

#### Port 5173 Already in Use
Edit `vite.config.js`:
```javascript
export default defineConfig({
  server: {
    port: 3000  // Use different port
  }
})
```

#### npm install Fails
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall
npm install
```

#### Dashboard Shows "Failed to fetch dashboard data"
1. **Ensure backend is running**: Check http://localhost:8080/api/dashboard/overview
2. **Check CORS settings**: Backend should allow localhost:5173
3. **Check browser console**: Look for network errors
4. **Verify proxy configuration**: Check vite.config.js proxy settings

#### Charts Not Rendering
1. **Check browser console**: Look for Chart.js errors
2. **Verify dependencies**: Run `npm install` again
3. **Clear browser cache**: Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)

---

### Network/CORS Issues

If you see CORS errors in the browser console:

**Backend CORS Configuration** (already configured in `CorsConfig.java`):
```java
.allowedOrigins(
    "http://localhost:3000",
    "http://localhost:5173",
    "http://localhost:8081"
)
```

Add your frontend port if using a different one.

## Development Tips

### Hot Reload
- **Backend**: Changes require restart (Ctrl+C, then `mvn spring-boot:run`)
- **Frontend**: Vite provides instant hot module replacement (HMR)

### Code Changes
- **Backend Java files**: Restart required
- **Frontend Vue files**: Auto-reload (instant)
- **Backend application.yml**: Restart required
- **Frontend vite.config.js**: Restart Vite server

### Debugging

#### Backend Debugging
```bash
# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.io.jeecloud.aidigitalsignage=DEBUG"
```

#### Frontend Debugging
- Open browser DevTools (F12)
- Check Console tab for errors
- Check Network tab for API calls
- Use Vue DevTools extension

## Testing

### Backend API Testing

Using curl:
```bash
curl http://localhost:8080/api/dashboard/overview
```

Using Postman or Insomnia:
- Method: GET
- URL: http://localhost:8080/api/dashboard/overview
- No authentication required

### Frontend Testing

```bash
cd microservices/digital-signage-dashboard

# Run tests (if configured)
npm run test
```

## Next Steps

### For Development
1. Modify mock data in `DashboardService.java`
2. Add new charts in `App.vue`
3. Create new DTO classes for additional metrics
4. Customize styling in `style.css`

### For Production
1. Add authentication (Spring Security)
2. Connect to real database (PostgreSQL, MongoDB)
3. Implement caching (Redis)
4. Add real-time updates (WebSocket)
5. Deploy to cloud (Azure, AWS, GCP)

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Vue 3 Documentation](https://vuejs.org/)
- [Chart.js Documentation](https://www.chartjs.org/)
- [Vite Documentation](https://vitejs.dev/)

## Support

For issues or questions:
1. Check the individual README files in each project folder
2. Review the troubleshooting section above
3. Check browser/terminal console for error messages

## License

This is an academic prototype for evaluation purposes.
