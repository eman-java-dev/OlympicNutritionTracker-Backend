package com.eman.tracker.olympicnutritiontracker.dto;

import jakarta.validation.constraints.*;   // ✅ للتحقق من القيم
import java.time.LocalDateTime;

public class ConsultationRequest {

    @NotBlank
    @Size(max = 300)
    private String message;

    @NotNull
    private LocalDateTime scheduledAt;

    @NotNull
    private Long athleteId;

    @NotNull
    private Long coachId;

    // ===== Getters / Setters =====
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public Long getAthleteId() { return athleteId; }
    public void setAthleteId(Long athleteId) { this.athleteId = athleteId; }

    public Long getCoachId() { return coachId; }
    public void setCoachId(Long coachId) { this.coachId = coachId; }
}
