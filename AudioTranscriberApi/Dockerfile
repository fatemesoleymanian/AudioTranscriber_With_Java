# Use Eclipse Temurin Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Install necessary packages (Alpine is minimal)
RUN apk add --no-cache ffmpeg alsa-lib alsa-utils

# Set working directory
WORKDIR /app

# Copy Maven build jar and resources
COPY target/AudioTranscriberApi-0.0.1-SNAPSHOT.jar app.jar

# Copy the Vosk model folder (make sure it's included in build)
COPY src/main/resources/models/ models/

# Set environment variable for model path
ENV VOSK_MODEL_PATH=models/vosk-model-small-en-us-0.15

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Dvosk.model.path=${VOSK_MODEL_PATH}", "-jar", "app.jar"]
