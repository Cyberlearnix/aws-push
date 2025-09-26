# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy api-gateway specific files
COPY api-gateway/build.gradle ./api-gateway/
COPY api-gateway/src ./api-gateway/src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :api-gateway:build -x test

# Copy the built JAR file (pick the boot jar deterministically)
RUN cp $(ls api-gateway/build/libs/*-SNAPSHOT.jar | head -n 1) app.jar

# Expose the port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
