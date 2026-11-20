package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.exception.ResourceNotFoundException;
import com.eman.tracker.olympicnutritiontracker.mapper.NutritionEntryMapper;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.NutritionEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NutritionEntryService {

    private final NutritionEntryRepository entries;
    private final AthleteRepository athletes;
    private final RecordAccess recordAccess;

    public NutritionEntryService(NutritionEntryRepository entries, AthleteRepository athletes, RecordAccess recordAccess) {
        this.entries = entries;
        this.athletes = athletes;
        this.recordAccess = recordAccess;
    }

    public Page<NutritionEntry> list(Pageable pageable) {
        return entries.findAll(recordAccess.current().nutritionEntries(), pageable);
    }

    public Page<NutritionEntry> listByAthlete(Long athleteId, Pageable pageable) {
        return entries.findAll(recordAccess.current().nutritionEntries()
                .and((root, query, builder) -> builder.equal(root.get("athlete").get("id"), athleteId)), pageable);
    }

    public NutritionEntry get(Long id) {
        return entries.findOne(recordAccess.current().nutritionEntries()
                        .and((root, query, builder) -> builder.equal(root.get("id"), id)))
                .orElseThrow(() -> new ResourceNotFoundException("NutritionEntry not found with id: " + id));
    }

    @Transactional
    public NutritionEntry create(NutritionEntryRequest req) {
        Athlete athlete = athletes.findById(req.getAthleteId())
                .orElseThrow(() -> new ResourceNotFoundException("Athlete not found with id: " + req.getAthleteId()));

        requireWriteAccess(athlete);
        NutritionEntry e = NutritionEntryMapper.toEntity(req, athlete);
        return entries.save(e);
    }

    @Transactional
    public NutritionEntry update(Long id, NutritionEntryRequest req) {
        NutritionEntry existing = get(id);

        Athlete athlete = athletes.findById(req.getAthleteId())
                .orElseThrow(() -> new ResourceNotFoundException("Athlete not found with id: " + req.getAthleteId()));

        requireWriteAccess(athlete);
        NutritionEntryMapper.copyForUpdate(req, existing, athlete);
        return entries.save(existing);
    }

    private void requireWriteAccess(Athlete athlete) {
        if (!recordAccess.current().canWriteFor(athlete)) {
            throw new AccessDeniedException("Nutrition entry is outside the coach's assignment");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!entries.existsById(id)) {
            throw new ResourceNotFoundException("NutritionEntry not found with id: " + id);
        }

        entries.deleteById(id);
    }
}