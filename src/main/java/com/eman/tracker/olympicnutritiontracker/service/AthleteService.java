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

@Service
public class AthleteService {

    private final AthleteRepository repo;
    private final CoachRepository coaches;

    public AthleteService(AthleteRepository repo, CoachRepository coaches) {
        this.repo = repo;
        this.coaches = coaches;
    }

    public Page<Athlete> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Athlete get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete not found with id: " + id));
    }

    @Transactional
    public Athlete create(Athlete a) {
        return repo.save(a);
    }

    @Transactional
    public Athlete update(Long id, Athlete data) {
        Athlete a = get(id);
        a.setName(data.getName());
        a.setAge(data.getAge());
        a.setGender(data.getGender());
        a.setHeight(data.getHeight());
        a.setWeight(data.getWeight());
        return repo.save(a);
    }

    // ✅ NEW: Assign Coach to Athlete
    @Transactional
    public Athlete assignCoach(Long athleteId, Long coachId) {
        Athlete athlete = get(athleteId);

        Coach coach = coaches.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        athlete.setCoach(coach);
        return repo.save(athlete);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Athlete not found with id: " + id);
        }
        repo.deleteById(id);
    }
}
