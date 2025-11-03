package com.eman.tracker.olympicnutritiontracker.dto;

import java.time.LocalDateTime;

public class ConsultationRequest {
    private String message;
    private LocalDateTime scheduledAt;
    private Long athleteId;
    private Long coachId;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public Long getAthleteId() { return athleteId; }
    public void setAthleteId(Long athleteId) { this.athleteId = athleteId; }

    public Long getCoachId() { return coachId; }
    public void setCoachId(Long coachId) { this.coachId = coachId; }
}
