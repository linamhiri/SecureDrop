# ==============================
# Stage 1: Build the application
# ==============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent Maven project descriptor
COPY pom.xml .

# Copy module POM files
COPY portal/pom.xml portal/pom.xml
COPY scanner/pom.xml scanner/pom.xml

# Copy scanner source code
COPY scanner/src scanner/src

# Build only the scanner module and required dependencies
RUN mvn -pl scanner -am clean package -DskipTests


# ==============================
# Stage 2: Run the application
# ==============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/scanner/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]