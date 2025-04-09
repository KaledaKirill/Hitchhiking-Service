package com.example.hitchhikingservice.service.impl;

import com.example.hitchhikingservice.exception.EntityNotFoundException;
import com.example.hitchhikingservice.exception.InternalServerErrorException;
import com.example.hitchhikingservice.service.LogService;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private static final String LOG_FILE_PATH = "logs/app.log";

    public Resource getLogsByDate(String date) {
        LocalDate logDate = parseDate(date);

        Path logFilePath = Paths.get(LOG_FILE_PATH);
        validateLogFileExists(logFilePath);

        String formattedDate = logDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        Path tempFile = createTempFile(logDate);
        filterAndWriteLogsToTempFile(logFilePath, formattedDate, tempFile);

        return createResourceFromTempFile(tempFile, date);
    }

    public LocalDate parseDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Required dd-MM-yyyy");
        }
    }

    public void validateLogFileExists(Path path) {
        if (!Files.exists(path)) {
            throw new EntityNotFoundException("File doesn't exist: " + LOG_FILE_PATH);
        }
    }

    public Path createTempFile(LocalDate logDate) {
        try {
            return Files.createTempFile("logs-" + logDate, ".log");
        } catch (IOException e) {
            throw new InternalServerErrorException("Error creating temp file: " + e.getMessage());
        }
    }

    public void filterAndWriteLogsToTempFile(Path logFilePath, String formattedDate,
                                              Path tempFile) {
        try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
            Files.write(tempFile, reader.lines()
                    .filter(line -> line.contains(formattedDate))
                    .toList());
        } catch (IOException e) {
            throw new InternalServerErrorException("Error processing log file: " + e.getMessage());
        }
    }

    public Resource createResourceFromTempFile(Path tempFile, String date) {
        try {
            if (Files.size(tempFile) == 0) {
                throw new EntityNotFoundException("There are no logs for specified date: " + date);
            }
            Resource resource = new UrlResource(tempFile.toUri());
            tempFile.toFile().deleteOnExit();
            return resource;
        } catch (IOException e) {
            throw new InternalServerErrorException("Error creating resource from temp file: "
                    + e.getMessage());
        }
    }
}