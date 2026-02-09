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

### Digital Signage Slideshow
- **Full-screen image rotation** every 5 seconds
- **Automatic playback** with smooth fade transitions
- **Manual controls** (previous/next/pause) on hover
- **Accessible via** http://localhost:5173/slideshow.html
- Perfect for displaying on digital signage screens

## Project Structure

```
digital-signage-dashboard/
├── index.html                  # Entry HTML file
├── slideshow.html              # Digital signage slideshow page
├── package.json                # Dependencies and scripts
├── vite.config.js              # Vite configuration
├── src/
│   ├── main.js                 # Application entry point
│   ├── slideshow.js            # Slideshow entry point
│   ├── App.vue                 # Main dashboard component
│   ├── style.css               # Global styles
│   ├── assets/                 # Static assets (images, banners)
│   │   ├── images/             # General images (logos, icons)
│   │   │   └── signage/        # Digital signage images
│   │   └── banners/            # Banner images for ads
│   ├── components/
│   │   ├── KpiCard.vue         # KPI card component
│   │   ├── BarChart.vue        # Bar chart component
│   │   ├── PieChart.vue        # Pie chart component
│   │   └── Slideshow.vue       # Slideshow component
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

**Dashboard**: http://localhost:5173  
**Slideshow**: http://localhost:5173/slideshow.html
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

## Using Assets (Images & Banners)

The dashboard includes an assets directory structure for images and banners:

### Path Aliases

Vite is configured with convenient path aliases:
- `@` → `./src`
- `@assets` → `./src/assets`
- `@images` → `./src/assets/images`
- `@banners` → `./src/assets/banners`

### Import Examples

```vue
<script setup>
// Import static images
import logo from '@images/logo.png'
import banner from '@banners/summer-sale.jpg'

// Or using dynamic imports
const getBannerPath = (filename) => {
  return new URL(`@banners/${filename}`, import.meta.url).href
}
</script>

<template>
  <img :src="logo" alt="Company Logo" />
  <img :src="banner" alt="Summer Sale" />
</template>
```

### Best Practices

- **Image Formats**: Use PNG for logos/icons, JPG/WebP for banners
- **Optimization**: Compress images before adding (keep under 500KB)
- **Naming**: Use kebab-case (e.g., `summer-sale-2026.jpg`)
- **Location**: 
  - `src/assets/images/` - Logos, icons, UI graphics
  - `src/assets/banners/` - Advertisement banners

See `src/assets/README.md` for detailed usage instructions.

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

## Digital Signage Slideshow

### Overview

The slideshow feature provides a full-screen, auto-rotating display perfect for digital signage screens. Images rotate every 5 seconds with smooth fade transitions.

### Accessing the Slideshow

**Development:**
```
http://localhost:5173/slideshow.html
```

**Production:**
```
https://your-domain.com/slideshow.html
```

### Features

- **Auto-rotation**: Images change every 5 seconds automatically
- **Full-screen display**: Optimized for digital signage screens
- **Smooth transitions**: Fade effect between slides
- **Manual controls**: Hover to reveal previous/next/pause buttons
- **Progress indicator**: Shows current slide number (e.g., "2 / 4")
- **Responsive**: Adapts to any screen size

### Adding Your Own Images

1. **Place images** in `src/assets/images/signage/` directory:
   ```bash
   src/assets/images/signage/
   ├── burger.png
   ├── coffee.png
   ├── pizza.png
   ├── salad.png
   └── your-new-image.jpg  # Add your image here
   ```

2. **Update the Slideshow component** (`src/components/Slideshow.vue`):
   ```javascript
   // Import your new image
   import yourImage from '@/assets/images/signage/your-new-image.jpg'
   
   // Add to the images array
   const images = [
     { src: burger, name: 'Delicious Burger' },
     { src: coffee, name: 'Fresh Coffee' },
     { src: pizza, name: 'Hot Pizza' },
     { src: salad, name: 'Healthy Salad' },
     { src: yourImage, name: 'Your Image Title' }  // Add this line
   ]
   ```

### Customization

**Change rotation speed** (default: 5 seconds):
```javascript
// In src/components/Slideshow.vue, line ~52
intervalId = setInterval(() => {
  nextSlide()
}, 5000) // Change this value (milliseconds)
```

**Change transition effect**:
```css
/* In src/components/Slideshow.vue, styles section */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.8s ease; /* Adjust duration/easing */
}
```

**Adjust image display**:
```css
/* In src/components/Slideshow.vue */
.slide {
  background-size: contain;  /* Options: contain, cover, 100% */
  background-position: center;
}
```

### Keyboard Controls

- **Space**: Pause/Play
- **Arrow Left**: Previous slide
- **Arrow Right**: Next slide
- **Esc**: Exit fullscreen (browser dependent)

### Deployment Tips

For dedicated signage screens:
1. Open the slideshow URL in fullscreen mode (F11)
2. Disable screen sleep/screensaver
3. Set browser to open slideshow URL on startup
4. Consider kiosk mode for security

**Chrome Kiosk Mode:**
```bash
chrome --kiosk --app=http://localhost:5173/slideshow.html
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
