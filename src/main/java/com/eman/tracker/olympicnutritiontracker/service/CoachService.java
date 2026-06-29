package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.exception.ResourceNotFoundException;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.CoachRepository;
import com.eman.tracker.olympicnutritiontracker.repository.ConsultationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoachService {

    private final CoachRepository coaches;
    private final AthleteRepository athletes;
    private final ConsultationRepository consultations;

    public CoachService(
            CoachRepository coaches,
            AthleteRepository athletes,
            ConsultationRepository consultations
    ) {
        this.coaches = coaches;
        this.athletes = athletes;
        this.consultations = consultations;
    }

    public Page<Coach> list(Pageable pageable) {
        return coaches.findAll(pageable);
    }

    public Coach getById(Long id) {
        return coaches.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));
    }

    @Transactional
    public Coach create(String name, String specialty, String email, String phone) {
        Coach c = new Coach();
        c.setName(name);
        c.setSpecialty(specialty);
        c.setEmail(email);
        c.setPhone(phone);

        return coaches.save(c);
    }

    @Transactional
    public Coach update(Long id, String name, String specialty, String email, String phone) {
        Coach existing = coaches.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));

        existing.setName(name);
        existing.setSpecialty(specialty);
        existing.setEmail(email);
        existing.setPhone(phone);

        return coaches.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Coach coach = coaches.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));

        consultations.deleteByCoach_Id(id);

        List<Athlete> linkedAthletes = athletes.findByCoach_Id(id);

        for (Athlete athlete : linkedAthletes) {
            athlete.setCoach(null);
        }

        athletes.saveAll(linkedAthletes);

        coaches.delete(coach);
    }

    public List<Coach> searchByName(String q) {
        if (q == null || q.isBlank()) {
            return coaches.findAll();
        }

        return coaches.findByNameContainingIgnoreCase(q.trim());
    }

    public List<Athlete> getAthletesByCoach(Long coachId) {
        coaches.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        return athletes.findByCoach_Id(coachId);
    }
}