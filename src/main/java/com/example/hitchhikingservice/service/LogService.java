package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.model.entity.LogObj;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface LogService {
    Long createLogAsync(String date);

    LogObj getStatus(Long taskId);

    ResponseEntity<Resource> downloadCreatedLogs(Long taskId) throws IOException;
}