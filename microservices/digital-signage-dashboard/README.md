# Audience Analysis Dashboard

Vue.js 3 dashboard for visualizing Edge AI digital signage audience analytics.

## Overview

This is a modern, responsive dashboard built with Vue 3 that displays real-time audience analytics from edge AI devices deployed in digital signage systems.

**Purpose**: Academic evaluation and SME demonstration  
**Data Source**: Mock data from backend API  
**Architecture**: Single Page Application (SPA)

## Technology Stack

- **Vue 3**: Progressive JavaScript framework
- **Composition API**: Modern reactive programming
- **Chart.js**: Data visualization library
- **Axios**: HTTP client for API calls
- **Vite**: Fast development build tool

## Features

### KPI Cards
- **Total Audience**: Unique visitor count
- **Total Views**: Content impression count
- **Total Ads**: Advertisement count
- **Average View Time**: Engagement duration in seconds

### Analytics Charts
1. **Age Distribution** - Bar chart showing demographic breakdown
2. **Advertisement Performance** - Horizontal bar chart of viewer counts per ad
3. **Gender Distribution** - Pie chart of male/female ratios
4. **Emotion Distribution** - Bar chart of detected facial expressions
5. **Advertisement Attention** - Horizontal bar chart of engagement metrics
6. **Overall Attention Rate** - Pie chart of look vs. no-look ratios

## Project Structure

```
digital-signage-dashboard/
├── index.html                  # Entry HTML file
├── package.json                # Dependencies and scripts
├── vite.config.js              # Vite configuration
├── src/
│   ├── main.js                 # Application entry point
│   ├── App.vue                 # Main dashboard component
│   ├── style.css               # Global styles
│   ├── components/
│   │   ├── KpiCard.vue         # KPI card component
│   │   ├── BarChart.vue        # Bar chart component
│   │   └── PieChart.vue        # Pie chart component
│   └── services/
│       └── api.js              # API service layer
```

## Getting Started

### Prerequisites
- Node.js 18+ or 20+
- npm or yarn package manager
- Backend service running on http://localhost:8080

### Installation

1. **Navigate to the project directory**
   ```bash
   cd microservices/digital-signage-dashboard
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

### Running the Development Server

```bash
npm run dev
```

The dashboard will be available at **http://localhost:5173**

### Building for Production

```bash
npm run build
```

This creates optimized production files in the `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## API Integration

The dashboard fetches data from a single API endpoint:

**Endpoint**: `GET /api/dashboard/overview`  
**Backend**: http://localhost:8080

The Vite dev server is configured with a proxy to forward `/api` requests to the backend.

### API Response Example

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
    }
  ],
  "adsAttention": [
    {
      "adName": "Summer Sale 2026",
      "lookYes": 388,
      "lookNo": 97
    }
  ]
}
```

## Component Documentation

### KpiCard.vue

Displays a single KPI metric.

**Props**:
- `title` (string, required) - Card title
- `value` (number/string, required) - Metric value
- `label` (string, optional) - Subtitle/unit label
- `format` (string, optional) - Format type: 'number', 'decimal', 'none'

**Usage**:
```vue
<KpiCard
  title="Total Audience"
  :value="1247"
  label="Unique Visitors"
  format="number"
/>
```

### BarChart.vue

Renders vertical or horizontal bar charts.

**Props**:
- `title` (string, required) - Chart title
- `labels` (array, required) - X-axis labels
- `data` (array, required) - Y-axis values
- `label` (string, optional) - Dataset label
- `horizontal` (boolean, optional) - Render horizontal bars
- `color` (string, optional) - Bar color (hex/rgb)

**Usage**:
```vue
<BarChart
  title="Age Distribution"
  :labels="['Children', 'Teenagers', 'Adults']"
  :data="[150, 225, 437]"
  label="People"
  color="#2563eb"
/>
```

### PieChart.vue

Renders pie charts for proportional data.

**Props**:
- `title` (string, required) - Chart title
- `labels` (array, required) - Slice labels
- `data` (array, required) - Slice values
- `colors` (array, optional) - Custom colors for slices

**Usage**:
```vue
<PieChart
  title="Gender Distribution"
  :labels="['Male', 'Female']"
  :data="[648, 599]"
  :colors="['#2563eb', '#ec4899']"
/>
```

## Configuration

### Vite Proxy Configuration

The `vite.config.js` includes a proxy to forward API requests:

```javascript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

This allows the frontend to call `/api/dashboard/overview` which proxies to the backend.

### Color Palette

The dashboard uses a consistent color scheme defined in CSS variables:

- **Primary**: `#2563eb` (Blue)
- **Success**: `#10b981` (Green)
- **Warning**: `#f59e0b` (Yellow)
- **Danger**: `#ef4444` (Red)
- **Info**: `#06b6d4` (Cyan)

## Responsive Design

The dashboard is fully responsive and adapts to different screen sizes:

- **Desktop**: Multi-column grid layout
- **Tablet**: 2-column grid
- **Mobile**: Single-column stacked layout

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## Troubleshooting

### Dashboard shows error message

**Issue**: "Failed to fetch dashboard data"

**Solution**:
1. Ensure the backend service is running on http://localhost:8080
2. Check browser console for CORS errors
3. Verify the backend endpoint is accessible: http://localhost:8080/api/dashboard/overview

### Charts not rendering

**Issue**: Blank chart areas

**Solution**:
1. Check browser console for Chart.js errors
2. Verify data format matches component props
3. Ensure canvas element has sufficient height

### Port 5173 already in use

**Issue**: Cannot start dev server

**Solution**:
```bash
# Kill the process using port 5173
# Or change the port in vite.config.js:
server: {
  port: 3000  // Use different port
}
```

## Development Notes

### Hot Module Replacement (HMR)
Vite provides instant updates during development. Changes to `.vue` files are reflected immediately without full page reload.

### State Management
This application uses Vue 3 Composition API with `ref` and `computed` for state management. For larger applications, consider Pinia or Vuex.

### API Service Layer
All API calls are centralized in `src/services/api.js` for maintainability and reusability.

## Future Enhancements

For production deployment, consider adding:
- Real-time data updates (WebSocket or polling)
- Historical data and trend analysis
- Date range filters
- Export functionality (PDF, Excel)
- User authentication and authorization
- Multi-location/device support
- Alerting and notifications
- Performance optimization with virtual scrolling

## License

This is an academic prototype for evaluation purposes.

## Contact

For questions or issues, please refer to the main project documentation.
