package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.exception.EntityNotFoundException;
import com.example.hitchhikingservice.exception.InternalServerErrorException;
import com.example.hitchhikingservice.service.impl.LogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    @InjectMocks
    private LogServiceImpl logService;

    @Test
    void parseDate_ValidFormat_ShouldReturnLocalDate() {
        String date = "15-01-2023";
        LocalDate result = logService.parseDate(date);
        assertEquals(LocalDate.of(2023, 1, 15), result);
    }

    @Test
    void parseDate_InvalidFormat_ShouldThrowBadRequestException() {
        String date = "2023-01-15";
        assertThrows(IllegalArgumentException.class, () -> logService.parseDate(date));
    }

    @Test
    void validateLogFileExists_WhenFileExists_ShouldNotThrowException() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path path = mock(Path.class);
            filesMock.when(() -> Files.exists(path)).thenReturn(true);

            logService.validateLogFileExists(path);
            // No exception expected
        }
    }

    @Test
    void validateLogFileExists_WhenFileNotExists_ShouldThrowNotFoundException() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path path = mock(Path.class);
            filesMock.when(() -> Files.exists(path)).thenReturn(false);

            assertThrows(EntityNotFoundException.class, () -> logService.validateLogFileExists(path));
        }
    }

    @Test
    void createTempFile_ShouldReturnPath() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path expectedPath = mock(Path.class);
            filesMock.when(() -> Files.createTempFile(anyString(), anyString()))
                    .thenReturn(expectedPath);

            Path result = logService.createTempFile(LocalDate.now());
            assertEquals(expectedPath, result);
        }
    }

    @Test
    void createTempFile_WhenIOException_ShouldThrowInternalServerError() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createTempFile(anyString(), anyString()))
                    .thenThrow(new IOException("Test error"));

            assertThrows(InternalServerErrorException.class,
                    () -> logService.createTempFile(LocalDate.now()));
        }
    }

    @Test
    void filterAndWriteLogsToTempFile_ShouldFilterByDate() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path logFilePath = mock(Path.class);
            Path tempFile = mock(Path.class);
            BufferedReader reader = mock(BufferedReader.class);

            filesMock.when(() -> Files.newBufferedReader(logFilePath)).thenReturn(reader);
            when(reader.lines()).thenReturn(Stream.of(
                    "15-01-2023: Test log 1",
                    "16-01-2023: Test log 2",
                    "15-01-2023: Test log 3"
            ));

            logService.filterAndWriteLogsToTempFile(logFilePath, "15-01-2023", tempFile);

            verify(reader).lines();
            filesMock.verify(() -> Files.write(eq(tempFile), eq(List.of(
                    "15-01-2023: Test log 1",
                    "15-01-2023: Test log 3"
            ))));
        }
    }

    @Test
    void createResourceFromTempFile_WhenFileNotEmpty_ShouldReturnResource() throws Exception {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path tempFile = mock(Path.class);
            File fileMock = mock(File.class);
            java.net.URI uri = new java.net.URI("file://temp.log");

            when(tempFile.toUri()).thenReturn(uri);
            when(tempFile.toFile()).thenReturn(fileMock);
            filesMock.when(() -> Files.size(tempFile)).thenReturn(100L);

            try (MockedConstruction<UrlResource> ignored = mockConstruction(UrlResource.class,
                    (mock, context) -> {
                        when(mock.exists()).thenReturn(true);
                        when(mock.isReadable()).thenReturn(true);
                    })) {

                Resource result = logService.createResourceFromTempFile(tempFile, "15-01-2023");

                assertNotNull(result);
                assertTrue(result.exists());
                assertTrue(result.isReadable());
                verify(fileMock).deleteOnExit();
            }
        }
    }

    @Test
    void createResourceFromTempFile_WhenFileEmpty_ShouldThrowNotFoundException() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path tempFile = mock(Path.class);
            filesMock.when(() -> Files.size(tempFile)).thenReturn(0L);

            assertThrows(EntityNotFoundException.class,
                    () -> logService.createResourceFromTempFile(tempFile, "15-01-2023"));
        }
    }

    @Test
    void getLogsByDate_IOExceptionInFiltering_ShouldThrowInternalServerError() {
        String date = "15-01-2023";
        Path logFilePath = Paths.get("logs/app.log");
        Path tempFile = mock(Path.class);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            pathsMock.when(() -> Paths.get("logs/app.log")).thenReturn(logFilePath);
            filesMock.when(() -> Files.exists(logFilePath)).thenReturn(true);
            filesMock.when(() -> Files.createTempFile(anyString(), anyString())).thenReturn(tempFile);
            filesMock.when(() -> Files.newBufferedReader(logFilePath)).thenThrow(new IOException("IO Error"));

            assertThrows(InternalServerErrorException.class, () -> logService.getLogsByDate(date));
        }
    }

    @Test
    void filterAndWriteLogsToTempFile_WhenIOException_ShouldThrowInternalServerError() {
        Path logFilePath = mock(Path.class);
        Path tempFile = mock(Path.class);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.newBufferedReader(logFilePath))
                    .thenThrow(new IOException("Reader error"));

            assertThrows(InternalServerErrorException.class,
                    () -> logService.filterAndWriteLogsToTempFile(logFilePath, "15-01-2023", tempFile));
        }
    }

    @Test
    void createResourceFromTempFile_WhenIOException_ShouldThrowInternalServerError() {
        Path tempFile = mock(Path.class);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.size(tempFile)).thenThrow(new IOException("Size error"));

            assertThrows(InternalServerErrorException.class,
                    () -> logService.createResourceFromTempFile(tempFile, "15-01-2023"));
        }
    }
}