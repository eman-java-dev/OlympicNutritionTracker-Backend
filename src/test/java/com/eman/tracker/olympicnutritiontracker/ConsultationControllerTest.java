package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.controller.ConsultationController;
import com.eman.tracker.olympicnutritiontracker.dto.ConsultationRequest;
import com.eman.tracker.olympicnutritiontracker.model.Consultation;
import com.eman.tracker.olympicnutritiontracker.service.ConsultationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for ConsultationController.
 * Security filters disabled for simplicity in tests.
 */
@WebMvcTest(ConsultationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsultationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ConsultationService consultationService;

    @Test
    void list_shouldReturnPage() throws Exception {
        Consultation c = new Consultation();
        c.setId(1L);
        c.setMessage("Hello");
        c.setScheduledAt(LocalDateTime.parse("2025-12-01T10:00:00"));

        Page<Consultation> page = new PageImpl<>(List.of(c));

        // استخدم any(Pageable.class) لتفادي تطابق صارم مع PageRequest
        Mockito.when(consultationService.list(any())).thenReturn(page);

        mockMvc.perform(get("/api/consultations"))
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        Consultation created = new Consultation();
        created.setId(10L);
        created.setMessage("New consult");
        created.setScheduledAt(LocalDateTime.parse("2025-12-01T10:00:00"));

        Mockito.when(consultationService.create(any(ConsultationRequest.class)))
                .thenReturn(created);

        String body = """
        {
          "message": "New consult",
          "scheduledAt": "2025-12-01T10:00:00",
          "athleteId": 1,
          "coachId": 1
        }
        """;

        mockMvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        Long id = 5L;

        Consultation updated = new Consultation();
        updated.setId(id);
        updated.setMessage("Rescheduled");
        updated.setScheduledAt(LocalDateTime.parse("2025-12-02T11:30:00"));

        Mockito.when(consultationService.update(eq(id), any(ConsultationRequest.class)))
                .thenReturn(updated);

        String body = """
        {
          "message": "Rescheduled",
          "scheduledAt": "2025-12-02T11:30:00",
          "athleteId": 1,
          "coachId": 2
        }
        """;

        mockMvc.perform(put("/api/consultations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        Long id = 7L;

        Mockito.doNothing().when(consultationService).delete(id);

        mockMvc.perform(delete("/api/consultations/{id}", id))
                .andExpect(status().isNoContent());
    }
}
