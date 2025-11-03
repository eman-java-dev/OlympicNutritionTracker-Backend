package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.mapper.NutritionEntryMapper;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.NutritionEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NutritionEntryService {

    private final NutritionEntryRepository entries;
    private final AthleteRepository athletes;

    public NutritionEntryService(NutritionEntryRepository entries, AthleteRepository athletes) {
        this.entries = entries;
        this.athletes = athletes;
    }

    public Page<NutritionEntry> list(Pageable pageable) {
        return entries.findAll(pageable);
    }

    public Page<NutritionEntry> listByAthlete(Long athleteId, Pageable pageable) {
        return entries.findByAthlete_Id(athleteId, pageable);
    }

    public NutritionEntry get(Long id) {
        return entries.findById(id)
                .orElseThrow(() -> new RuntimeException("NutritionEntry not found with id: " + id));
    }

    public NutritionEntry create(NutritionEntryRequest req) {
        Athlete athlete = athletes.findById(req.getAthleteId())
                .orElseThrow(() -> new RuntimeException("Athlete not found with id: " + req.getAthleteId()));
        NutritionEntry e = NutritionEntryMapper.toEntity(req, athlete);
        return entries.save(e);
    }

    public NutritionEntry update(Long id, NutritionEntryRequest req) {
        NutritionEntry existing = get(id);
        Athlete athlete = athletes.findById(req.getAthleteId())
                .orElseThrow(() -> new RuntimeException("Athlete not found with id: " + req.getAthleteId()));
        NutritionEntryMapper.copyForUpdate(req, existing, athlete);
        return entries.save(existing);
    }

    public void delete(Long id) {
        if (!entries.existsById(id)) {
            throw new RuntimeException("NutritionEntry not found with id: " + id);
        }
        entries.deleteById(id);
    }
}
