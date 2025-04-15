package com.example.hitchhikingservice.service.impl;

import com.example.hitchhikingservice.exception.EntityNotFoundException;
import com.example.hitchhikingservice.model.entity.LogObj;
import com.example.hitchhikingservice.service.LogService;
import com.example.hitchhikingservice.utils.ErrorMessages;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private final AtomicLong idCounter = new AtomicLong(1);
    private final Map<Long, LogObj> tasks = new ConcurrentHashMap<>();
    private static final String LOG_FILE_PATH = "logs/app.log";
    private final LogServiceImpl self;

    public LogServiceImpl(@Lazy LogServiceImpl self) {
        this.self = self;
    }

    @Async("executor")
    public void createLogs(Long taskId, String date) {
        try {
            Thread.sleep(10000);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate logDate = LocalDate.parse(date, formatter);

            Path path = Paths.get(LOG_FILE_PATH);
            List<String> logLines = Files.readAllLines(path);
            String formattedDate = logDate.format(formatter);
            List<String> currentLogs = logLines.stream()
                    .filter(line -> line.startsWith(formattedDate))
                    .toList();

            if (currentLogs.isEmpty()) {
                LogObj logObject = tasks.get(taskId);
                if (logObject != null) {
                    logObject.setStatus("FAILED");
                    logObject.setErrorMessage("There is not logs for date: " + date);
                }
                throw new IllegalArgumentException("There is not logs for date: " + date);
            }

            Path logFile;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                logFile = Files.createTempFile("logs-" + formattedDate, ".log");
            } else {
                FileAttribute<Set<PosixFilePermission>> attr =
                        PosixFilePermissions.asFileAttribute(
                                PosixFilePermissions.fromString("rwx------")
                        );
                logFile = Files.createTempFile("logs-" + formattedDate, ".log", attr);
            }

            Files.write(logFile, currentLogs);
            logFile.toFile().deleteOnExit();

            LogObj task = tasks.get(taskId);
            if (task != null) {
                task.setStatus("COMPLETED");
                task.setFilePath(logFile.toString());
            }
        } catch (IOException e) {
            LogObj task = tasks.get(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setErrorMessage(e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Long createLogAsync(String date) {
        Long id = idCounter.getAndIncrement();
        LogObj logObject = new LogObj(id, "IN_PROGRESS");
        tasks.put(id, logObject);
        self.createLogs(id, date);
        return id;
    }

    public LogObj getStatus(Long taskId) {
        return tasks.get(taskId);
    }

    public ResponseEntity<Resource> downloadCreatedLogs(Long taskId) throws IOException {
        LogObj logObject = getStatus(taskId);
        if (logObject == null) {
            throw new EntityNotFoundException(ErrorMessages.FILE_NOT_FOUND);
        }
        if (!"COMPLETED".equals(logObject.getStatus())) {
            throw new IllegalArgumentException("The logs are not ready yet");
        }

        Path path = Paths.get(logObject.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}