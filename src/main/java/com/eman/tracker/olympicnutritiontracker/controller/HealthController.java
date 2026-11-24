package com.eman.tracker.olympicnutritiontracker.controller;

import javax.sql.DataSource;
import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping({"/health", "/healthz"})
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/readyz")
    public ResponseEntity<Void> readiness() {
        try (var connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return ResponseEntity.ok().build();
            }
        } catch (SQLException ignored) {
            // Public health responses carry only a status, including on dependency failures.
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
