package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.dto.ConsultationRequest;
import com.eman.tracker.olympicnutritiontracker.exception.ResourceNotFoundException;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.model.Consultation;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.CoachRepository;
import com.eman.tracker.olympicnutritiontracker.repository.ConsultationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<Consultation> list(Pageable pageable) {
        return consultations.findAll(pageable);
    }

    public Consultation get(Long id) {
        return consultations.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Consultation not found with id: " + id));
    }

    @Transactional
    public Consultation create(ConsultationRequest req) {
        Athlete athlete = athletes.findById(req.getAthleteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Athlete not found with id: " + req.getAthleteId()));

        Coach coach = coaches.findById(req.getCoachId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Coach not found with id: " + req.getCoachId()));

        Consultation c = new Consultation();
        c.setMessage(req.getMessage());
        c.setScheduledAt(req.getScheduledAt());
        c.setAthlete(athlete);
        c.setCoach(coach);

        return consultations.save(c);
    }

    @Transactional
    public Consultation update(Long id, ConsultationRequest req) {
        Consultation existing = get(id);

        Athlete athlete = athletes.findById(req.getAthleteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Athlete not found with id: " + req.getAthleteId()));

        Coach coach = coaches.findById(req.getCoachId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Coach not found with id: " + req.getCoachId()));

        existing.setMessage(req.getMessage());
        existing.setScheduledAt(req.getScheduledAt());
        existing.setAthlete(athlete);
        existing.setCoach(coach);

        return consultations.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Consultation existing = get(id);
        consultations.delete(existing);
    }
}