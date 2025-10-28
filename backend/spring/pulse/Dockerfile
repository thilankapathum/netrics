# ========================
# 1. Build stage
# ========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set working directory inside container
WORKDIR /app

# Copy pom.xml and download dependencies first (to cache dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ========================
# 2. Runtime stage
# ========================
FROM eclipse-temurin:21-jre-alpine

ARG PROFILE=prod

# Set working directory
WORKDIR /app

# Copy only the packaged JAR from the build stage
COPY --from=build /app/target/pulse*.jar pulse.jar

# Expose the port (default Spring Boot is 8080)
EXPOSE 8012

ENV ACTIVE_PROFILE=${PROFILE}

# Run the application
#ENTRYPOINT ["java","-jar","app.jar"]
CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} pulse.jar
