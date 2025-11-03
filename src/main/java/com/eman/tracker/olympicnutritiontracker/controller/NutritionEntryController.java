package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryResponse;
import com.eman.tracker.olympicnutritiontracker.mapper.NutritionEntryMapper;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;
import com.eman.tracker.olympicnutritiontracker.service.NutritionEntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nutrition-entries")
public class NutritionEntryController {

    private final NutritionEntryService service;

    public NutritionEntryController(NutritionEntryService service) {
        this.service = service;
    }

    // GET all
    @GetMapping
    public Page<NutritionEntryResponse> list(Pageable pageable) {
        return service.list(pageable).map(NutritionEntryMapper::toResponse);
    }

    // GET by athlete
    @GetMapping("/by-athlete/{athleteId}")
    public Page<NutritionEntryResponse> listByAthlete(@PathVariable Long athleteId, Pageable pageable) {
        return service.listByAthlete(athleteId, pageable).map(NutritionEntryMapper::toResponse);
    }

    // GET by id
    @GetMapping("/{id}")
    public NutritionEntryResponse get(@PathVariable Long id) {
        NutritionEntry e = service.get(id);
        return NutritionEntryMapper.toResponse(e);
    }

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NutritionEntryResponse create(@RequestBody NutritionEntryRequest req) {
        return NutritionEntryMapper.toResponse(service.create(req));
    }

    // UPDATE
    @PutMapping("/{id}")
    public NutritionEntryResponse update(@PathVariable Long id, @RequestBody NutritionEntryRequest req) {
        return NutritionEntryMapper.toResponse(service.update(id, req));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
