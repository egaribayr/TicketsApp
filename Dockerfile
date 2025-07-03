# Use an official Gradle image to build the application
FROM gradle:8.5.0-jdk21 AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle build -x test

# Use a lightweight JDK image to run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]