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

# Copy module specific files
COPY payment-service/build.gradle ./payment-service/
COPY payment-service/src ./payment-service/src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :payment-service:build -x test

# Copy the built JAR file
RUN cp payment-service/build/libs/*.jar app.jar

# Expose the port
EXPOSE 8086

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8086/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
