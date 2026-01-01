package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.exception.ResourceNotFoundException;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.CoachRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoachService {

    private final CoachRepository coaches;
    private final AthleteRepository athletes;

    public CoachService(CoachRepository coaches, AthleteRepository athletes) {
        this.coaches = coaches;
        this.athletes = athletes;
    }

    // ✅ List coaches (paged)
    public Page<Coach> list(Pageable pageable) {
        return coaches.findAll(pageable);
    }

    // ✅ Get coach by id
    public Coach getById(Long id) {
        return coaches.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));
    }

    // ✅ Create coach
    @Transactional
    public Coach create(String name, String specialty) {
        Coach c = new Coach();
        c.setName(name);
        c.setSpecialty(specialty);
        return coaches.save(c);
    }

    // ✅ Update coach
    @Transactional
    public Coach update(Long id, String name, String specialty) {
        Coach existing = coaches.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));

        existing.setName(name);
        existing.setSpecialty(specialty);
        return coaches.save(existing);
    }

    // ✅ Delete coach
    @Transactional
    public void delete(Long id) {
        if (!coaches.existsById(id)) {
            throw new ResourceNotFoundException("Coach not found with id: " + id);
        }
        coaches.deleteById(id);
    }

    // ✅ Optional: search by name
    public List<Coach> searchByName(String q) {
        if (q == null || q.isBlank()) return coaches.findAll();
        return coaches.findByNameContainingIgnoreCase(q.trim());
    }

    // ✅ Athletes by coach
    public List<Athlete> getAthletesByCoach(Long coachId) {
        // تأكيد وجود الكوتش (حتى لو ما عنده Athletes)
        coaches.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        return athletes.findByCoach_Id(coachId);
    }
}
