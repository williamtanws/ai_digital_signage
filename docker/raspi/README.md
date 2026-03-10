# AI Digital Signage - Raspberry Pi Docker Deployment

## 📋 Overview

Docker Compose configuration optimized for **Raspberry Pi 4/5** to run the complete AI Digital Signage system:

- **TDengine** - Time-series database for analytics data
- **Analytics ETL Service** - Spring Boot service (Java 21)
- **Digital Signage Service** - Spring Boot service with SQLite (Java 21)
- **Digital Signage Dashboard** - Vue.js frontend with Nginx

## 🔧 Prerequisites

### Hardware
- **Raspberry Pi 4** (4GB+ RAM) or **Raspberry Pi 5** (8GB+ recommended)
- **64-bit OS** (ARM64 architecture required)
- **SD Card**: 32GB+ recommended
- **Network**: Stable internet connection

### Software
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group
sudo usermod -aG docker $USER

# Install Docker Compose (if not included)
sudo apt-get update
sudo apt-get install docker-compose-plugin

# Verify installation
docker --version
docker compose version
```

## 🚀 Quick Start

### Option 1: Build and Run (Recommended)

```bash
# Navigate to docker/raspi directory
cd /path/to/ai_digital_signage/docker/raspi

# Build all services (first time - takes 10-20 minutes on Pi)
docker compose build

# Start all services
docker compose up -d

# View logs
docker compose logs -f
```

### Option 2: Pre-built JARs (Faster)

If you've already built the JAR files locally:

```bash
# Build JARs on host (or development machine)
cd /path/to/ai_digital_signage/microservices

# Build digital-signage-service
cd digital-signage-service
./mvnw clean package -DskipTests
cd ..

# Build analytics-etl-service
cd analytics-etl-service
./mvnw clean package -DskipTests
cd ..

# Build dashboard
cd digital-signage-dashboard
npm install
npm run build
cd ..

# Return to docker directory and start
cd ../docker/raspi
docker compose up -d
```

## 📊 Service Ports

| Service | Port | URL |
|---------|------|-----|
| Dashboard | 80 | http://localhost |
| Digital Signage API | 8080 | http://localhost:8080/api |
| Analytics ETL API | 8081 | http://localhost:8081 |
| TDengine REST API | 6041 | http://localhost:6041 |
| TDengine Native | 6030 | - |

## 🔍 Monitoring & Management

### Check Service Status
```bash
# View all containers
docker compose ps

# View logs
docker compose logs -f

# View specific service logs
docker compose logs -f digital-signage-service
docker compose logs -f analytics-etl-service
docker compose logs -f digital-signage-dashboard
docker compose logs -f tdengine
```

### Health Checks
```bash
# Check service health
docker compose ps

# Test endpoints
curl http://localhost:8080/actuator/health  # Digital Signage Service
curl http://localhost:8081/actuator/health  # Analytics ETL Service
curl http://localhost                       # Dashboard
curl http://localhost:6041/rest/sql         # TDengine
```

### Resource Monitoring
```bash
# View resource usage
docker stats

# View specific container
docker stats ai-signage-service
```

## 🛠️ Management Commands

### Start/Stop Services
```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# Restart a specific service
docker compose restart digital-signage-service

# Stop without removing containers
docker compose stop

# Start stopped containers
docker compose start
```

### Update and Rebuild
```bash
# Rebuild specific service
docker compose build digital-signage-service

# Rebuild and restart
docker compose up -d --build digital-signage-service

# Force rebuild without cache
docker compose build --no-cache
```

### Data Management
```bash
# Backup volumes
docker run --rm -v raspi_signage-data:/data -v $(pwd):/backup \
  alpine tar czf /backup/signage-data-backup.tar.gz /data

# List volumes
docker volume ls

# Remove unused volumes (CAUTION: deletes data)
docker volume prune
```

## 🎯 Performance Optimization

### Memory Settings (Already Optimized)

**Service Memory Limits:**
- TDengine: 1GB (512MB reserved)
- Analytics ETL: 512MB (256MB reserved)
- Digital Signage: 576MB (256MB reserved)
- Dashboard: 128MB (64MB reserved)

**Total: ~2.2GB** (suitable for Pi 4 with 4GB+ RAM)

### CPU Settings
- Services use G1GC for better memory management
- CPU limits prevent any single service from monopolizing resources
- String deduplication enabled to reduce memory footprint

### Adjusting Resources
Edit `docker-compose.yml` to increase/decrease resources:

```yaml
deploy:
  resources:
    limits:
      memory: 512M      # Increase if needed
      cpus: '1.0'       # Adjust CPU allocation
```

## 🐛 Troubleshooting

### Services Not Starting
```bash
# Check logs
docker compose logs

# Check individual service
docker compose logs digital-signage-service

# Restart services
docker compose restart
```

### Out of Memory Errors
```bash
# Check memory usage
free -h
docker stats

# Reduce service memory or stop other processes
# Edit docker-compose.yml memory limits
```

### Build Failures
```bash
# Clean Docker build cache
docker builder prune

# Remove old images
docker image prune -a

# Rebuild from scratch
docker compose build --no-cache
```

### TDengine Connection Issues
```bash
# Check TDengine is running
docker compose ps tdengine

# Check TDengine logs
docker compose logs tdengine

# Test TDengine connection
docker compose exec tdengine taos -s "show databases;"
```

### Permission Issues
```bash
# Fix volume permissions
sudo chown -R 1000:1000 ../../microservices/digital-signage-service/data
sudo chown -R 1000:1000 ../../microservices/analytics-etl-service/data
```

## 📁 Volume Persistence

**Docker Volumes:**
- `tdengine-data` - TDengine database files
- `tdengine-log` - TDengine logs
- `signage-data` - SQLite database

**Host Mounts:**
- `../../microservices/analytics-etl-service/data` - ETL metadata
- Shared with host for easy backup

## 🔒 Security Notes

- All services run as non-root users (UID 1000)
- No privileged containers
- Network isolation via Docker bridge network
- Health checks for service monitoring
- Resource limits prevent resource exhaustion

## 🚀 Production Recommendations

1. **Use environment variables** for sensitive configuration
2. **Set up log rotation** to prevent disk fill
3. **Enable automatic restart** (already configured: `restart: unless-stopped`)
4. **Monitor disk space** regularly
5. **Backup volumes** periodically
6. **Use Docker secrets** for passwords (if using Docker Swarm)
7. **Set up monitoring** (Prometheus/Grafana)

## 📖 Additional Resources

- [Digital Signage Service README](../../microservices/digital-signage-service/README.md)
- [Analytics ETL Service README](../../microservices/analytics-etl-service/README.md)
- [Dashboard README](../../microservices/digital-signage-dashboard/README.md)
- [TDengine Documentation](https://docs.tdengine.com/)

## 🆘 Getting Help

If you encounter issues:

1. Check the logs: `docker compose logs`
2. Verify health checks: `docker compose ps`
3. Check resource usage: `docker stats`
4. Review this README's troubleshooting section

## 📝 Notes

- **First build** takes 10-20 minutes on Raspberry Pi
- **Subsequent builds** use cache and are much faster
- Services start in dependency order (TDengine → Digital Signage → ETL → Dashboard)
- All services use **Pi-optimized profiles** automatically
- Dashboard is accessible at **http://localhost** (port 80)

## 🔄 Updates

To update services:

```bash
# Pull latest changes
cd /path/to/ai_digital_signage
git pull

# Rebuild and restart
cd docker/raspi
docker compose up -d --build
```

---

**Optimized for Raspberry Pi 4/5 | ARM64 Architecture | Low Memory Footprint**
