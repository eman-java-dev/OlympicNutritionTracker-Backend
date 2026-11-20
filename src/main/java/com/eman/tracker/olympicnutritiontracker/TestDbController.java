package com.eman.tracker.olympicnutritiontracker;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("!prod")
@RestController
@RequestMapping("/api")
public class TestDbController {

    private final JdbcTemplate jdbc;

    public TestDbController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/db-check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> checkDatabaseConnection() {
        try {
            if (Integer.valueOf(1).equals(jdbc.queryForObject("SELECT 1", Integer.class))) {
                return ResponseEntity.ok("Database connection available");
            }
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Database connection unavailable");
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Database connection unavailable");
    }
}
