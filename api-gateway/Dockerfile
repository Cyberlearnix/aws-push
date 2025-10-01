# Stage 1: Build
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app

# Copy root Gradle wrapper and configs
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

# Copy all modules
COPY api-gateway ./api-gateway
COPY user-service ./user-service
COPY order-service ./order-service
# ... add other modules as needed

# Make gradlew executable
RUN chmod +x ./gradlew

# Build API Gateway module only
RUN ./gradlew :api-gateway:bootJar -x test --no-daemon --stacktrace

# Stage 2: Runtime
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built jar from builder
COPY --from=builder /app/api-gateway/build/libs/api-gateway-*.jar ./api-gateway.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
