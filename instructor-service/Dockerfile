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

# Copy instructor-service specific files
COPY instructor-service/build.gradle ./instructor-service/
COPY instructor-service/src ./instructor-service/src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :instructor-service:build -x test

# Copy the built JAR file deterministically
RUN JAR_FILE=$(ls instructor-service/build/libs/*-SNAPSHOT.jar 2>/dev/null || ls instructor-service/build/libs/*.jar | head -n 1) \
    && cp "$JAR_FILE" app.jar

# Expose the port
EXPOSE 8083

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
