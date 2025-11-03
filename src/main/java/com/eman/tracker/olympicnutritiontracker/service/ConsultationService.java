package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.model.Consultation;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.CoachRepository;
import com.eman.tracker.olympicnutritiontracker.repository.ConsultationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationService {

    private final ConsultationRepository consultations;
    private final AthleteRepository athletes;
    private final CoachRepository coaches;

    public ConsultationService(ConsultationRepository consultations,
                               AthleteRepository athletes,
                               CoachRepository coaches) {
        this.consultations = consultations;
        this.athletes = athletes;
        this.coaches = coaches;
    }

    // 🟢 عرض كل الاستشارات (مع التصفح Pagination)
    public Page<Consultation> list(Pageable pageable) {
        return consultations.findAll(pageable);
    }

    // 🟢 إنشاء استشارة جديدة
    public Consultation create(String message, LocalDateTime scheduledAt, Long athleteId, Long coachId) {
        Athlete athlete = athletes.findById(athleteId)
                .orElseThrow(() -> new RuntimeException("Athlete not found with id: " + athleteId));

        Coach coach = coaches.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found with id: " + coachId));

        Consultation c = new Consultation();
        c.setMessage(message);
        c.setScheduledAt(scheduledAt);
        c.setAthlete(athlete);
        c.setCoach(coach);

        return consultations.save(c);
    }

    // 🟡 تحديث استشارة موجودة
    public Consultation update(Long id, String message, LocalDateTime scheduledAt, Long athleteId, Long coachId) {
        Consultation existing = consultations.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found with id: " + id));

        Athlete athlete = athletes.findById(athleteId)
                .orElseThrow(() -> new RuntimeException("Athlete not found with id: " + athleteId));

        Coach coach = coaches.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found with id: " + coachId));

        existing.setMessage(message);
        existing.setScheduledAt(scheduledAt);
        existing.setAthlete(athlete);
        existing.setCoach(coach);

        return consultations.save(existing);
    }

    //  حذف استشارة حسب الـ id
    public void delete(Long id) {
        if (!consultations.existsById(id)) {
            throw new RuntimeException("Consultation not found with id: " + id);
        }
        consultations.deleteById(id);
    }
}
