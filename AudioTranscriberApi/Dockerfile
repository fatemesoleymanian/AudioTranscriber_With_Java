# ======= Stage 1: Build =======
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ======= Stage 2: Run =======
FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache ffmpeg alsa-lib alsa-utils

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/AudioTranscriberApi-0.0.1-SNAPSHOT.jar app.jar

COPY src/main/resources/models/ models/

ENV VOSK_MODEL_PATH=models/vosk-model-small-en-us-0.15

EXPOSE 8080

ENTRYPOINT ["java", "-Dvosk.model.path=${VOSK_MODEL_PATH}", "-jar", "app.jar"]
