package com.example.hitchhikingservice.service;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import com.example.hitchhikingservice.model.entity.LogObj;

public interface LogService {
    Long createLogAsync(String date);

    LogObj getStatus(Long taskId);

    ResponseEntity<Resource> downloadCreatedLogs(Long taskId) throws IOException;
}