package ru.netology.cloudStorage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("service", "Cloud Storage Service");
        status.put("version", "1.0.0");

        log.debug("Health check requested");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Cloud Storage Service");
        info.put("description", "REST service for file storage with authentication");
        info.put("version", "1.0.0");
        info.put("author", "Denis Lipatov");
        info.put("contact", "ya@denislipatov.ru");

        return ResponseEntity.ok(info);
    }
}
