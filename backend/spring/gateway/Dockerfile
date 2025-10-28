#LABEL authors="THILANKAW"
#
#FROM eclipse-temurin:21-jre-alpine
#
#WORKDIR /app
#
#COPY target/gateway-*.jar gateway.jar
#
#EXPOSE 8001
#
##ENTRYPOINT ["java", "-jar", "app.jar"]
#CMD java -jar gateway.jar


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

# Set working directory
WORKDIR /app

# Copy only the packaged JAR from the build stage
COPY --from=build /app/target/gateway*.jar gateway.jar

# Expose the port (default Spring Boot is 8080)
EXPOSE 8001

# Run the application
ENTRYPOINT ["java","-jar","gateway.jar"]