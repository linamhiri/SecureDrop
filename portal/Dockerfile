# ==============================
# Stage 1: Build the application
# ==============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the parent Maven project descriptor
COPY pom.xml .

# Copy module POM files
COPY portal/pom.xml portal/pom.xml
COPY scanner/pom.xml scanner/pom.xml

# Copy portal source code
COPY portal/src portal/src

# Build only the portal module and any required dependencies
RUN mvn -pl portal -am clean package -DskipTests


# ==============================
# Stage 2: Run the application
# ==============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/portal/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]