#!/bin/bash
# ==============================================
# Monitor AI Digital Signage Services
# ==============================================

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

clear

echo "=================================="
echo "AI Digital Signage - System Monitor"
echo "=================================="
echo ""

# Function to check service health
check_health() {
    local service=$1
    local url=$2
    
    if curl -sf "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} $service"
    else
        echo -e "${RED}✗${NC} $service"
    fi
}

# Function to format bytes
format_bytes() {
    local bytes=$1
    if [ $bytes -lt 1024 ]; then
        echo "${bytes}B"
    elif [ $bytes -lt 1048576 ]; then
        echo "$(($bytes / 1024))KB"
    elif [ $bytes -lt 1073741824 ]; then
        echo "$(($bytes / 1048576))MB"
    else
        echo "$(($bytes / 1073741824))GB"
    fi
}

# Continuous monitoring loop
while true; do
    clear
    echo "=================================="
    echo -e "${BLUE}AI Digital Signage - System Monitor${NC}"
    echo "=================================="
    echo ""
    
    # Container status
    echo -e "${YELLOW}Container Status:${NC}"
    docker compose ps --format "table {{.Name}}\t{{.Status}}"
    echo ""
    
    # Service health
    echo -e "${YELLOW}Service Health:${NC}"
    check_health "Dashboard        " "http://localhost:80/health"
    check_health "Digital Signage  " "http://localhost:8080/actuator/health"
    check_health "Analytics ETL    " "http://localhost:8081/actuator/health"
    check_health "TDengine         " "http://localhost:6041/rest/sql"
    echo ""
    
    # Resource usage
    echo -e "${YELLOW}Resource Usage:${NC}"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" | grep -E "NAME|ai-signage"
    echo ""
    
    # System resources
    echo -e "${YELLOW}System Resources:${NC}"
    echo -e "Memory: $(free -h | awk '/^Mem:/ {print $3"/"$2" ("int($3/$2*100)"%)"}')"
    echo -e "CPU:    $(top -bn1 | grep "Cpu(s)" | awk '{print $2+$4"%"}')"
    echo -e "Disk:   $(df -h / | awk 'NR==2 {print $3"/"$2" ("$5")"}')"
    echo ""
    
    # Refresh info
    echo -e "${BLUE}Press Ctrl+C to exit | Refreshing in 5 seconds...${NC}"
    sleep 5
done
