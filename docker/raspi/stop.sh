#!/bin/bash
# ==============================================
# AI Digital Signage - Raspberry Pi Stop Script
# ==============================================

set -e  # Exit on error

echo "=================================="
echo "AI Digital Signage - Shutdown"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Navigate to docker/raspi directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if containers are running
if ! docker compose ps --services --filter "status=running" | grep -q .; then
    echo -e "${YELLOW}No running containers found${NC}"
    echo ""
    docker compose ps
    exit 0
fi

echo -e "${YELLOW}Stopping services...${NC}"
echo ""

# Show current status
docker compose ps

echo ""
echo -e "${YELLOW}Stopping containers gracefully...${NC}"

# Stop containers (gives them time to shutdown gracefully)
docker compose stop

# Optional: Remove containers (uncomment if you want to remove containers on stop)
# echo ""
# echo -e "${YELLOW}Removing stopped containers...${NC}"
# docker compose down

echo ""
echo "=================================="
echo -e "${GREEN}✓ Services Stopped${NC}"
echo "=================================="
echo ""
echo "Container status:"
docker compose ps
echo ""
echo "To start again, run: ./start.sh"
echo "To remove containers and volumes: docker compose down -v"
echo ""
