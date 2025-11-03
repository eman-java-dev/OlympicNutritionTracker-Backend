package com.eman.tracker.olympicnutritiontracker.mapper;

import com.eman.tracker.olympicnutritiontracker.dto.ConsultationResponse;
import com.eman.tracker.olympicnutritiontracker.model.Consultation;

public final class ConsultationMapper {
    private ConsultationMapper() {}

    public static ConsultationResponse toResponse(Consultation c) {
        if (c == null) return null;
        ConsultationResponse r = new ConsultationResponse();
        r.setId(c.getId());
        r.setMessage(c.getMessage());
        r.setScheduledAt(c.getScheduledAt());
        if (c.getAthlete() != null) {
            r.setAthleteId(c.getAthlete().getId());
            r.setAthleteName(c.getAthlete().getName());
        }
        if (c.getCoach() != null) {
            r.setCoachId(c.getCoach().getId());
            r.setCoachName(c.getCoach().getName());
        }
        return r;
    }
}
