package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.exception.ResourceNotFoundException;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.CoachRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AthleteService {

    private final AthleteRepository repo;
    private final CoachRepository coaches;
    private final RecordAccess recordAccess;

    public AthleteService(AthleteRepository repo, CoachRepository coaches, RecordAccess recordAccess) {
        this.repo = repo;
        this.coaches = coaches;
        this.recordAccess = recordAccess;
    }

    public Page<Athlete> list(Pageable pageable) {
        return repo.findAll(recordAccess.current().athletes(), pageable);
    }

    public Athlete get(Long id) {
        return repo.findOne(recordAccess.current().athletes()
                        .and((root, query, builder) -> builder.equal(root.get("id"), id)))
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
        try {
            repo.deleteById(id);
            repo.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This athlete cannot be deleted because existing records, such as consultations or nutrition entries, are linked to them.",
                    ex);
        }
    }
}
