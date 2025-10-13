# Use Eclipse Temurin 21 JRE Alpine as base image for better security and smaller size
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install curl for health checks (Alpine uses apk)
RUN apk add --no-cache curl

# Copy the executable JAR file (relative path from service directory)
COPY build/libs/user-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
