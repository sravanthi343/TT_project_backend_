# Stage 1: Build the JAR using Maven
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
# Copy only the files needed for the build
COPY pom.xml .
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR using Java 17
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy the generated JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]