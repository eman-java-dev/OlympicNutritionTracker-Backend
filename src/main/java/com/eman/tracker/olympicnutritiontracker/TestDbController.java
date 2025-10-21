package com.eman.tracker.olympicnutritiontracker;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestDbController {

    private final JdbcTemplate jdbc;

    public TestDbController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/db-check")
    public String checkDatabaseConnection() {
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            if (one != null && one == 1) {
                return "✅ Connexion réussie à la base de données Olympic Nutrition Tracker !";
            } else {
                return "⚠️ Connexion établie, mais réponse inattendue.";
            }
        } catch (Exception e) {
            return "❌ Erreur de connexion : " + e.getMessage();
        }
    }
}
