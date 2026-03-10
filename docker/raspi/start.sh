#!/bin/bash
# ==============================================
# AI Digital Signage - Raspberry Pi Quick Start
# ==============================================

set -e  # Exit on error

echo "=================================="
echo "AI Digital Signage - Pi Deployment"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    echo "Install Docker: curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh"
    exit 1
fi

# Check if Docker Compose is available
if ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not installed${NC}"
    echo "Install: sudo apt-get install docker-compose-plugin"
    exit 1
fi

echo -e "${GREEN}✓ Docker is installed${NC}"
echo ""

# Navigate to docker/raspi directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "Current directory: $SCRIPT_DIR"
echo ""

# Check if JARs need to be built
SIGNAGE_JAR="../../microservices/digital-signage-service/target/digital-signage-service-1.0.0-SNAPSHOT.jar"
ETL_JAR="../../microservices/analytics-etl-service/target/analytics-etl-service-1.0.0-SNAPSHOT.jar"
DASHBOARD_DIST="../../microservices/digital-signage-dashboard/dist"

BUILD_NEEDED=0

if [ ! -f "$SIGNAGE_JAR" ]; then
    echo -e "${YELLOW}⚠ Digital Signage Service JAR not found${NC}"
    BUILD_NEEDED=1
fi

if [ ! -f "$ETL_JAR" ]; then
    echo -e "${YELLOW}⚠ Analytics ETL Service JAR not found${NC}"
    BUILD_NEEDED=1
fi

if [ ! -d "$DASHBOARD_DIST" ]; then
    echo -e "${YELLOW}⚠ Dashboard build not found${NC}"
    BUILD_NEEDED=1
fi

if [ $BUILD_NEEDED -eq 1 ]; then
    echo ""
    echo -e "${YELLOW}Building will happen inside Docker containers${NC}"
    echo -e "${YELLOW}This will take 10-20 minutes on first run${NC}"
    echo ""
    read -p "Continue? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Stop existing containers
echo -e "${YELLOW}Stopping existing containers...${NC}"
docker compose down

# Pull base images
echo ""
echo -e "${YELLOW}Pulling base images...${NC}"
docker compose pull || true

# Build images
echo ""
echo -e "${YELLOW}Building application images...${NC}"
docker compose build

# Start services
echo ""
echo -e "${GREEN}Starting services...${NC}"
docker compose up -d

# Wait for services to be healthy
echo ""
echo -e "${YELLOW}Waiting for services to start...${NC}"
sleep 10

# Check service status
docker compose ps

# Display useful information
echo ""
echo "=================================="
echo -e "${GREEN}✓ Deployment Complete!${NC}"
echo "=================================="
echo ""
echo "Service URLs:"
echo "  Dashboard:        http://localhost:5173"
echo "  Digital Signage:  http://localhost:8080/api"
echo "  Analytics ETL:    http://localhost:8081"
echo "  TDengine:         http://localhost:6041"
echo ""
echo "Useful commands:"
echo "  View logs:        docker compose logs -f"
echo "  Stop services:    docker compose down"
echo "  Restart:          docker compose restart"
echo "  Status:           docker compose ps"
echo ""
echo -e "${GREEN}System is ready!${NC}"
