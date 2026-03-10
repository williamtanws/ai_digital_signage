# AI Digital Signage - Quick Reference Commands

## 🚀 Quick Start

### Using Scripts (Easiest)
```bash
# Linux/Mac
./start.sh

# Windows
.\start.ps1
```

### Manual Commands

#### First Time Setup
```bash
# Build all images
docker compose build

# Start services
docker compose up -d

# View logs
docker compose logs -f
```

#### Daily Operations
```bash
# Start all services
docker compose start

# Stop all services
docker compose stop

# Restart all services
docker compose restart

# View status
docker compose ps

# View logs (follow mode)
docker compose logs -f

# View specific service logs
docker compose logs -f digital-signage-service
```

## 🔧 Service Management

### Restart Individual Services
```bash
docker compose restart digital-signage-service
docker compose restart analytics-etl-service
docker compose restart digital-signage-dashboard
docker compose restart tdengine
```

### Rebuild Individual Services
```bash
# After code changes
docker compose build digital-signage-service
docker compose up -d digital-signage-service

# Force rebuild without cache
docker compose build --no-cache digital-signage-service
```

### Update Services
```bash
# Pull latest code
git pull

# Rebuild and restart specific service
docker compose up -d --build digital-signage-service

# Rebuild and restart all
docker compose up -d --build
```

## 📊 Monitoring

### Resource Usage
```bash
# All containers
docker stats

# Specific container
docker stats ai-signage-service

# System monitor (Linux/Mac)
./monitor.sh
```

### Health Checks
```bash
# Check all services
docker compose ps

# Test endpoints directly
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost
```

### Logs
```bash
# All logs
docker compose logs

# Follow logs
docker compose logs -f

# Last 100 lines
docker compose logs --tail=100

# Specific service with timestamp
docker compose logs -f --timestamps digital-signage-service
```

## 🗄️ Database Management

### TDengine
```bash
# Connect to TDengine CLI
docker compose exec tdengine taos

# Inside taos CLI:
show databases;
use analytics_db;
show stables;
select count(*) from gaze_events;
```

### SQLite (Digital Signage)
```bash
# Access SQLite database
docker compose exec digital-signage-service sqlite3 /app/data/digital-signage.db

# Inside SQLite:
.tables
.schema advertisements
SELECT * FROM advertisements LIMIT 10;
```

## 💾 Backup & Restore

### Backup Volumes
```bash
# Backup TDengine data
docker run --rm \
  -v raspi_tdengine-data:/data \
  -v $(pwd)/backups:/backup \
  alpine tar czf /backup/tdengine-$(date +%Y%m%d).tar.gz /data

# Backup SQLite database
docker compose exec digital-signage-service \
  cp /app/data/digital-signage.db /app/data/digital-signage-backup-$(date +%Y%m%d).db
```

### Restore Volumes
```bash
# Restore TDengine data
docker run --rm \
  -v raspi_tdengine-data:/data \
  -v $(pwd)/backups:/backup \
  alpine tar xzf /backup/tdengine-20260308.tar.gz -C /
```

## 🧹 Cleanup

### Clean Up Containers
```bash
# Stop and remove containers
docker compose down

# Stop, remove containers and volumes (CAUTION: deletes data)
docker compose down -v

# Remove old/unused images
docker image prune -a

# Clean build cache
docker builder prune
```

### Reset Everything
```bash
# Complete cleanup (CAUTION: deletes all data)
docker compose down -v
docker system prune -a --volumes
```

## 🔍 Debugging

### Access Container Shell
```bash
# Digital Signage Service
docker compose exec digital-signage-service sh

# Analytics ETL Service
docker compose exec analytics-etl-service sh

# TDengine
docker compose exec tdengine bash

# Dashboard
docker compose exec digital-signage-dashboard sh
```

### Check Environment Variables
```bash
docker compose exec digital-signage-service env | grep SPRING
docker compose exec analytics-etl-service env | grep JAVA
```

### Network Inspection
```bash
# List networks
docker network ls

# Inspect signage network
docker network inspect raspi_signage-network

# Test connectivity between services
docker compose exec digital-signage-service ping tdengine
docker compose exec analytics-etl-service wget -O- http://digital-signage-service:8080/actuator/health
```

## 📈 Performance Tuning

### Adjust Memory Limits
Edit `docker-compose.yml`:
```yaml
deploy:
  resources:
    limits:
      memory: 512M  # Increase/decrease as needed
```

Then restart:
```bash
docker compose up -d
```

### View Current Resource Limits
```bash
docker inspect ai-signage-service | grep -A 10 Resources
```

## 🌐 Network Access

### Access from Another Device
If you want to access the dashboard from another device on the same network:

1. Find Pi's IP address:
   ```bash
   hostname -I
   ```

2. Access from another device:
   - Dashboard: `http://<pi-ip-address>`
   - API: `http://<pi-ip-address>:8080/api`

### Port Forwarding (Optional)
If you want external access, configure your router to forward ports to your Pi.

## 🔐 Security Tips

```bash
# Change default ports in docker-compose.yml
ports:
  - "8888:80"  # Change dashboard port

# Use environment variables for passwords
# Create .env file from .env.template
cp .env.template .env
# Edit .env with your secure values
nano .env
```

## 📱 Useful URLs

After starting services:
- Dashboard: http://localhost
- Slideshow: http://localhost/slideshow.html
- Digital Signage API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- Analytics ETL: http://localhost:8081
- TDengine REST: http://localhost:6041

---

**For more detailed information, see [README.md](README.md)**
