FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the pre-built JAR
COPY user-service/build/libs/user-service-0.0.1-SNAPSHOT.jar ./user-service.jar

# Expose the port your service runs on
EXPOSE 8081

# Run the JAR
ENTRYPOINT ["java", "-jar", "user-service.jar"]
