package com.eman.tracker.olympicnutritiontracker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class NutritionEntryService {

    private final NutritionEntryRepository repo;
    private final AthleteRepository athleteRepo;

    public NutritionEntryService(NutritionEntryRepository repo, AthleteRepository athleteRepo) {
        this.repo = repo;
        this.athleteRepo = athleteRepo;
    }

    public List<NutritionEntry> getAll() {
        return repo.findAll();
    }

    public List<NutritionEntry> getByAthlete(Long athleteId) {
        return repo.findByAthleteId(athleteId);
    }

    public NutritionEntry get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nutrition entry id=" + id + " not found"));
    }

    public NutritionEntry create(Long athleteId, NutritionEntry entry) {
        Athlete athlete = athleteRepo.findById(athleteId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete id=" + athleteId + " not found"));
        entry.setAthlete(athlete);
        return repo.save(entry);
    }

    public NutritionEntry update(Long id, NutritionEntry incoming) {
        NutritionEntry existing = get(id);
        if (incoming.getDate() != null) existing.setDate(incoming.getDate());
        if (incoming.getMealType() != null) existing.setMealType(incoming.getMealType());
        if (incoming.getCalories() != null) existing.setCalories(incoming.getCalories());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(get(id));
    }
}

