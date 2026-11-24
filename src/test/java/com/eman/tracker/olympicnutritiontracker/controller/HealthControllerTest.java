package com.eman.tracker.olympicnutritiontracker.controller;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HealthControllerTest {
    @Test
    void livenessDoesNotBorrowConnectionAndReadinessHidesFailureDetails() throws Exception {
        DataSource source = mock(DataSource.class);
        HealthController health = new HealthController(source);
        assertThat(health.health().getStatusCode().value()).isEqualTo(200);
        verifyNoInteractions(source);
        when(source.getConnection()).thenThrow(new SQLException("synthetic database exception"));
        var response = health.readiness();
        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNull();
    }
}
