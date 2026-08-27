# ==============================
# Stage 1: Build Scanner
# ==============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY portal/pom.xml portal/pom.xml
COPY scanner/pom.xml scanner/pom.xml
COPY scanner/src scanner/src

RUN mvn -pl scanner -am clean package -DskipTests


# ==============================
# Stage 2: Azure Scanner + ClamAV
# ==============================
FROM clamav/clamav:1.5

USER root

RUN apk add --no-cache openjdk17-jre-headless

WORKDIR /app

COPY --from=build /app/scanner/target/*.jar app.jar

COPY scanner/azure-entrypoint.sh /azure-entrypoint.sh

RUN chmod +x /azure-entrypoint.sh

RUN sed -i 's/^#\?TCPAddr .*/TCPAddr 127.0.0.1/' /etc/clamav/clamd.conf

ENTRYPOINT ["/azure-entrypoint.sh"]