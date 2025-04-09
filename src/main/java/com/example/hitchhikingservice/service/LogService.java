package com.example.hitchhikingservice.service;

import java.nio.file.Path;
import java.time.LocalDate;
import org.springframework.core.io.Resource;

public interface LogService {
    Resource getLogsByDate(String date);

    LocalDate parseDate(String date);

    void validateLogFileExists(Path path);

    Path createTempFile(LocalDate logDate);

    void filterAndWriteLogsToTempFile(Path logFilePath, String formattedDate, Path tempFile);

    Resource createResourceFromTempFile(Path tempFile, String date);
}
