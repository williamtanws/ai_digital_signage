# Edge AI–Powered Digital Signage for Audience Analytics

**Master's Dissertation Project**  
*A Prototype System for Privacy-Preserving Audience Analysis in SME Environments*

---

## Project Overview

This repository contains the prototype implementation supporting a master's dissertation that investigates the **feasibility and adoption readiness of edge AI-powered digital signage systems** for small and medium-sized enterprise (SME) food and beverage environments.

The system demonstrates:

- **On-device audience analysis** using edge AI models (face detection, demographic estimation, attention tracking, emotion recognition)
- **Privacy-by-design architecture** with no raw image storage or cloud transmission
- **Real-time analytics processing** at the edge with minimal latency
- **Modular, service-oriented architecture** suitable for SME operational constraints
- **Local, on-premise deployment** requiring no cloud infrastructure dependencies

### Research Context

Traditional digital signage systems lack audience measurement capabilities, while cloud-based analytics solutions raise privacy concerns and require continuous connectivity. This research explores whether edge AI can provide meaningful audience insights while preserving privacy and operating reliably in resource-constrained SME settings.

The prototype validates:
- Technical feasibility of real-time edge AI inference on modest hardware
- Privacy-preserving data collection and aggregation approaches
- System architecture suitable for SME operational requirements
- Practical deployment considerations for food and beverage venues

---

## System Architecture

The system consists of three primary components that interact to collect, process, and visualize audience analytics:

### Architecture Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Edge Device (Raspberry Pi / Embedded System)                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  audience-analysis-service (Python)                       │  │
│  │  - Captures video frames from camera                      │  │
│  │  - Runs edge AI models (face detection, demographics)     │  │
│  │  - Extracts non-identifying analytics only                │  │
│  │  - Writes structured logs to TDengine time-series DB      │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ Local Network
                         │ (Analytics Data)
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  Application Server (Lightweight PC / NUC)                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  digital-signage-service (Spring Boot)                    │  │
│  │  - Aggregates analytics from TDengine                     │  │
│  │  - Provides REST API for dashboard                        │  │
│  │  - Handles reporting and configuration                    │  │
│  │  - Manages operational state                              │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ HTTP REST API
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  Staff Browser (Desktop / Tablet)                               │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  digital-signage-dashboard (Vue.js)                       │  │
│  │  - Displays audience analytics and KPIs                   │  │
│  │  - Visualizes trends and distributions                    │  │
│  │  - Provides operational control interface                 │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Component Interaction

1. **Edge AI Analysis Layer** (`audience-analysis-service`)
   - Runs on edge hardware (Raspberry Pi, embedded system)
   - Processes camera input in real-time using locally deployed AI models
   - Discards raw images immediately after analysis
   - Logs only aggregated, non-identifying analytics (counts, demographics, attention metrics)
   - Stores time-series data in TDengine for efficient querying

2. **Application Service Layer** (`digital-signage-service`)
   - Runs on a lightweight server or shared infrastructure
   - Queries TDengine for analytics data
   - Aggregates and transforms data for reporting
   - Exposes REST API endpoints consumed by the dashboard
   - Manages system configuration and operational state

3. **Presentation Layer** (`digital-signage-dashboard`)
   - Browser-based Vue.js application
   - Accessed by venue staff for monitoring and reporting
   - Displays real-time and historical audience analytics
   - Provides visualizations (charts, KPIs, trends)
   - Communicates exclusively with `digital-signage-service` via REST API

**Key Architectural Principles:**
- **Privacy-first**: No raw images stored; only non-identifying aggregate data
- **Edge-first**: AI inference happens locally, no cloud dependency
- **Modular**: Each component can be developed, tested, and deployed independently
- **Lightweight**: Designed for SME constraints (cost, expertise, infrastructure)

---

## Repository Structure

```
ai_digital_signage/
│
├── microservices/
│   ├── audience-analysis-service/      # Python-based edge AI service
│   │   - Real-time face detection and analysis
│   │   - Demographic estimation (age, gender)
│   │   - Attention and emotion tracking
│   │   - Privacy-preserving data logging
│
├── microservices/
│   │
│   ├── digital-signage-service/        Backend Service (Spring Boot)
│   │   ├── src/main/java/.../
│   │   │   ├── controller/             REST API endpoints
│   │   │   ├── service/                Business logic & data aggregation
│   │   │   ├── dto/                    Data transfer objects
│   │   │   └── config/                 CORS and application configuration
│   │   ├── pom.xml                     Maven dependencies
│   │   └── README.md                   Service-specific documentation
│   │
│   ├── digital-signage-dashboard/      Frontend Dashboard (Vue.js)
│   │   ├── src/
│   │   │   ├── components/             Reusable UI components (KPI cards, charts)
│   │   │   ├── services/               API service layer
│   │   │   ├── App.vue                 Main dashboard view
│   │   │   └── style.css               Global styles
│   │   ├── package.json                npm dependencies
│   │   ├── vite.config.js              Build configuration
│   │   └── README.md                   Dashboard-specific documentation
│   │
│   ├── GETTING_STARTED.md              Quick start guide for all services
│   ├── PROJECT_SUMMARY.md              Project overview and architecture
│   └── sample-api-response.json        Example API response format
│
├── models/                              Edge AI Model Files
│   ├── retinaface_mobilenet.../        Face detection model
│   ├── yolov8n_relu6_age.../           Age estimation model
│   ├── yolov8n_relu6_fairface_gender.../ Gender classification model
│   ├── emotion_recognition_fer2013.../  Emotion recognition model
│   └── tddfa_mobilenet_v1.../          Head pose estimation model
│
├── pipeline/                            Development & Testing Notebooks
│   ├── 000_audience_analysis_live.ipynb  Live analysis pipeline
│   ├── 001_quick_start.ipynb            Quick start examples
│   └── development.ipynb                Development testing
│
├── logs/                                Log Files (gitignored in production)
│
└── README.md                            This file

```

### Component Descriptions

#### `audience-analysis-service` (Python)
**Purpose**: Performs real-time audience analysis using edge AI models.

**Key Functions**:
- Captures video frames from connected cameras
- Runs inference using quantized AI models optimized for edge devices
- Detects faces, estimates demographics (age, gender), tracks attention and emotions
- Aggregates data over configurable time windows
- Logs structured, non-identifying data to TDengine time-series database
- Ensures privacy by discarding raw images immediately

**Technologies**: Python, OpenCV, Hailo AI accelerator SDK, TDengine client

---

#### `digital-signage-service` (Spring Boot)
**Purpose**: Central operational service providing analytics aggregation and API access.

**Key Functions**:
- Queries TDengine for audience analytics data
- Aggregates and transforms data for reporting and visualization
- Exposes RESTful API endpoints for dashboard consumption
- Manages system configuration and operational parameters
- Handles authentication and access control (if implemented)

**Technologies**: Java 21, Spring Boot 3.5.10, Maven, Lombok

**API Endpoints**:
- `GET /api/dashboard/overview` - Returns comprehensive dashboard metrics

---

#### `digital-signage-dashboard` (Vue.js)
**Purpose**: Web-based admin interface for staff to monitor audience analytics.

**Key Functions**:
- Displays real-time and historical audience metrics
- Visualizes data using interactive charts (Chart.js)
- Presents KPIs: total audience, total views, average engagement time
- Shows demographic distributions (age, gender)
- Displays emotion analysis and advertisement performance metrics

**Technologies**: Vue 3, Composition API, Chart.js, Axios, Vite

**Dashboard Features**:
- 4 KPI cards (audience count, views, ads, average time)
- 6 analytics charts (age distribution, gender split, emotions, ad performance)
- Responsive layout suitable for desktop and tablet access
- Real-time data refresh from backend API

---

## Technology Stack

### Edge AI & Analytics Layer
- **Python 3.x** - Core programming language for edge service
- **OpenCV** - Computer vision and image processing
- **Hailo AI SDK** - Hardware acceleration for edge AI inference
- **TDengine** - Time-series database for analytics data storage

### Backend Service Layer
- **Java 21 (LTS)** - Programming language
- **Spring Boot 3.5.10** - Application framework
- **Maven 3.9+** - Build and dependency management
- **Lombok** - Boilerplate code reduction

### Frontend Presentation Layer
- **Vue.js 3** - Progressive JavaScript framework
- **Composition API** - Modern reactive state management
- **Chart.js** - Data visualization library
- **Axios** - HTTP client for API communication
- **Vite** - Fast build tool and dev server

### Data Storage
- **TDengine** - Time-series database for analytics logs
- **SQLite** (optional) - Lightweight relational database for configuration

### Edge AI Models
- **RetinaFace MobileNet** - Face detection
- **YOLOv8n** - Age and gender classification
- **FER2013 Emotion Recognition** - Facial emotion detection
- **TDDFA MobileNetV1** - Head pose estimation (attention tracking)

All models are quantized and optimized for efficient inference on edge hardware.

---

## How to Run (Prototype Demonstration)

> **Note**: These instructions are for demonstration and evaluation purposes only. This is prototype code supporting academic research, not production-ready software.

### Prerequisites

**Hardware**:
- Edge device with camera (e.g., Raspberry Pi 4/5 with Hailo AI accelerator)
- Application server (PC, NUC, or shared infrastructure)
- Staff device with web browser

**Software**:
- Python 3.9+ (edge service)
- Java 21+ (backend service)
- Node.js 18+ (frontend dashboard)
- TDengine instance (local or shared)

### Quick Start

#### 1. Set Up Edge AI Service

```bash
# Navigate to edge service directory
cd microservices/audience-analysis-service

# Install Python dependencies
pip install -r requirements.txt

# Configure camera and TDengine connection
# Edit configuration file with connection details

# Run the service
python audience_analysis_live.py
```

The edge service will begin processing camera input and logging analytics.

---

#### 2. Start Backend Service

```bash
# Navigate to backend service directory
cd microservices/digital-signage-service

# Build the Spring Boot application
mvn clean package

# Run the service
mvn spring-boot:run
```

The backend API will be available at: `http://localhost:8080`

**API Endpoint**: `GET http://localhost:8080/api/dashboard/overview`

---

#### 3. Start Frontend Dashboard

```bash
# Navigate to dashboard directory
cd microservices/digital-signage-dashboard

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

The dashboard will be available at: `http://localhost:5173`

---

### Component-Specific Documentation

Each service includes detailed setup and usage instructions:

- **Backend**: See [`microservices/digital-signage-service/README.md`](microservices/digital-signage-service/README.md)
- **Frontend**: See [`microservices/digital-signage-dashboard/README.md`](microservices/digital-signage-dashboard/README.md)
- **Quick Start Guide**: See [`microservices/GETTING_STARTED.md`](microservices/GETTING_STARTED.md)

---

## Privacy Considerations

This system is designed with **privacy-by-design** principles:

### What is NOT Stored
- ❌ Raw camera images or video streams
- ❌ Facial images or biometric templates
- ❌ Personal identifying information (PII)
- ❌ Individual tracking or identity linkage

### What IS Stored
- ✅ Aggregated demographic counts (age groups, gender distribution)
- ✅ Anonymized attention metrics (viewing duration, engagement levels)
- ✅ Emotional state distributions (neutral, happy, serious, surprised)
- ✅ Time-series analytics (trends over time, peak periods)

### Privacy Mechanisms
1. **Immediate Image Disposal**: Raw frames are processed and discarded within milliseconds
2. **Edge Processing**: All AI inference occurs locally; no data leaves the premises
3. **Aggregate-Only Logging**: Only statistical summaries are recorded, never individual records
4. **No Cloud Dependency**: No external servers or third-party services receive any data
5. **Configurable Retention**: Analytics data can be purged on configurable schedules

This approach aligns with GDPR principles and minimizes privacy risks for both customers and venue operators.

---

## Research & Academic Disclaimer

### Purpose
This repository contains code developed as part of a **master's dissertation research project**. The primary purpose is to:
- Demonstrate technical feasibility of edge AI-powered audience analytics
- Validate system architecture for SME deployment scenarios
- Provide a working prototype for academic evaluation and examination

### Limitations
This is **not production-ready software**. Known limitations include:
- **Limited error handling** and edge case coverage
- **Mock data** used in some components for development and testing
- **Minimal security hardening** (authentication, authorization, encryption)
- **No formal quality assurance** or testing coverage
- **Prototype-level documentation** rather than enterprise-grade guides
- **No deployment automation** or DevOps tooling
- **Limited scalability** and performance optimization

### Intended Use
This code is intended for:
- ✅ Academic evaluation by dissertation examiners and supervisors
- ✅ Technical review by research peers
- ✅ Demonstration of proof-of-concept capabilities
- ✅ Basis for further research and development

This code is **NOT intended for**:
- ❌ Production deployment in commercial environments
- ❌ Mission-critical or safety-critical applications
- ❌ Large-scale or high-availability systems
- ❌ Unmodified use without proper engineering review

### Future Work
For production deployment, significant additional work would be required:
- Comprehensive security hardening and threat modeling
- Robust error handling and fault tolerance
- Performance optimization and load testing
- User authentication and role-based access control
- Monitoring, logging, and alerting infrastructure
- Backup and disaster recovery procedures
- Compliance with relevant regulations and standards
- Professional code review and quality assurance

---

## License

This project is developed for academic purposes as part of a master's dissertation. Please contact the author for any usage inquiries.

---

## Acknowledgments

This research was conducted as part of a master's program investigating the practical application of edge AI technologies in SME contexts. The project explores the intersection of artificial intelligence, privacy-preserving computing, and digital signage systems.

### Technologies & Tools
- Edge AI models sourced from open research and optimized for embedded deployment
- Hardware acceleration provided by Hailo AI accelerators
- Open-source frameworks: Spring Boot, Vue.js, TDengine

---

## Contact

For questions regarding this research or codebase, please refer to the dissertation documentation or contact the author through academic channels.

---

**Last Updated**: February 2026  
**Repository Type**: Master's Dissertation Prototype  
**Status**: Active Development / Evaluation Phase