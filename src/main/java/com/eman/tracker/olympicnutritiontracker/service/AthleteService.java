package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class AthleteService {
    private final AthleteRepository repo;
    public AthleteService(AthleteRepository repo) { this.repo = repo; }

    public Page<Athlete> list(Pageable pageable) { return repo.findAll(pageable); }

    public Athlete get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Athlete not found: " + id));
    }

    public Athlete create(Athlete a) { return repo.save(a); }

    public Athlete update(Long id, Athlete data) {
        Athlete a = get(id);
        a.setName(data.getName());
        a.setAge(data.getAge());
        a.setGender(data.getGender());
        a.setHeight(data.getHeight());
        a.setWeight(data.getWeight());
        return repo.save(a);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
