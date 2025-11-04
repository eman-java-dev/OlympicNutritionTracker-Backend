package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.controller.ConsultationController;
import com.eman.tracker.olympicnutritiontracker.dto.ConsultationResponse;
import com.eman.tracker.olympicnutritiontracker.service.ConsultationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultationController.class)
@AutoConfigureMockMvc(addFilters = false) // ⬅️ تعطيل فلاتر Security في هذا الاختبار فقط
class ConsultationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ConsultationService consultationService;

    @Test
    void list_shouldReturnPage() throws Exception {
        ConsultationResponse r = new ConsultationResponse();
        r.setId(1L);
        r.setMessage("Hello");
        r.setScheduledAt(LocalDateTime.now());
        r.setAthleteId(1L);
        r.setCoachId(1L);

        Mockito.when(consultationService.list(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(

                )));

        mockMvc.perform(get("/api/consultations")) // ?page=0&size=20 تلقائيًا
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        ConsultationResponse created = new ConsultationResponse();
        created.setId(10L);
        created.setMessage("New consult");
        created.setScheduledAt(LocalDateTime.parse("2025-12-01T10:00:00"));
        created.setAthleteId(1L);
        created.setCoachId(1L);

        Mockito.when(consultationService.create(
                Mockito.anyString(),
                Mockito.any(LocalDateTime.class),
                Mockito.anyLong(),
                Mockito.anyLong()
        )).thenReturn(

                null
        );

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
}
