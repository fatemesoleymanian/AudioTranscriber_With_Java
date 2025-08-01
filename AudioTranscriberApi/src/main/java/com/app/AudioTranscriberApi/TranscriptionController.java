package com.app.AudioTranscriberApi;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.*;

@RestController
@RequestMapping("/api/transcribe")
@CrossOrigin
public class TranscriptionController {

    private Model model;

    @Value("${vosk.model.path}")
    private String modelPath;

    @PostConstruct
    public void initModel() throws IOException {
        model = new Model(modelPath);
    }

    @PostMapping
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) {
        File tempFile = null;

        try {
            // Save uploaded file to a temp WAV file
            tempFile = File.createTempFile("audio", ".wav");
            file.transferTo(tempFile);

            // Get original audio stream
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(tempFile);
            AudioFormat originalFormat = originalStream.getFormat();

            // Define the required Vosk format: 16kHz, mono, 16-bit PCM, little endian
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    16000,
                    16,
                    1,
                    2,
                    16000,
                    false
            );

            // Convert audio if needed
            AudioInputStream convertedStream = AudioSystem.isConversionSupported(targetFormat, originalFormat)
                    ? AudioSystem.getAudioInputStream(targetFormat, originalStream)
                    : null;

            if (convertedStream == null) {
                return ResponseEntity.badRequest().body("Cannot convert audio to 16kHz mono PCM format.");
            }

            // Transcribe using Vosk
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = convertedStream.read(buffer)) >= 0) {
                recognizer.acceptWaveForm(buffer, bytesRead);
            }

            String result = recognizer.getFinalResult();
            return ResponseEntity.ok(result);

        } catch (UnsupportedAudioFileException e) {
            return ResponseEntity.badRequest().body("Unsupported audio format: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transcription failed: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
