#!/bin/bash
#
# Start Analytics ETL Service on Raspberry Pi 5
# Optimized for ARM64, 16GB RAM
#

set -e

# JVM Memory Settings (Conservative for 16GB system)
# Allocate max 1GB to ETL service
export JAVA_OPTS="-Xms128m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -Djava.awt.headless=true \
  -Dfile.encoding=UTF-8"

# Set working directory
cd "$(dirname "$0")"

# Ensure data directory exists
mkdir -p ./data

# Build the service
echo "Building analytics-etl-service..."
mvn clean package -DskipTests -q

# Check if build succeeded
if [ $? -ne 0 ]; then
  echo "Build failed!"
  exit 1
fi

echo "Starting analytics-etl-service on port 8081..."
echo "JVM Memory: 128MB initial, 1GB max"
echo "Profile: pi (Raspberry Pi optimized)"
echo ""

# Start service with Pi profile
java $JAVA_OPTS \
  -jar target/analytics-etl-service-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=pi

