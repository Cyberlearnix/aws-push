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

# Copy config-server specific files
COPY config-server/build.gradle ./config-server/
COPY config-server/src ./config-server/src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :config-server:build -x test

# Copy the built JAR file
RUN cp config-server/build/libs/*.jar app.jar

# Expose the port
EXPOSE 8888

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
