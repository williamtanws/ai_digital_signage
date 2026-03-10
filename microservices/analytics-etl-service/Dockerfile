# ==============================================
# Analytics ETL Service - Raspberry Pi Optimized
# ==============================================
# Multi-stage build for minimal image size
# ARM64 compatible for Raspberry Pi 4/5

# Stage 1: Build (optional - can build outside Docker)
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copy Maven wrapper and pom.xml for dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src src
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

# Install wget for healthchecks
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JAR from builder or use ARG for pre-built JAR
ARG JAR_FILE=target/analytics-etl-service-1.0.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Create data directory
RUN mkdir -p /app/data

# Use non-root user for security
RUN useradd -r -u 1000 appuser && \
    chown -R appuser:appuser /app
USER appuser

EXPOSE 8081

# JVM optimizations for ARM64 and low memory
ENV JAVA_OPTS="-Xmx384m -Xms128m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-pi} -jar app.jar"]
