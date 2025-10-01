FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR from repo root
COPY instructor-service/build/libs/instructor-service-0.0.1-SNAPSHOT.jar ./instructor-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "instructor-service.jar"]
