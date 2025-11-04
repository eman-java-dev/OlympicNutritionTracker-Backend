package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryResponse;
import com.eman.tracker.olympicnutritiontracker.mapper.NutritionEntryMapper;
import com.eman.tracker.olympicnutritiontracker.service.NutritionEntryService;
import jakarta.validation.Valid;
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

    @GetMapping
    public Page<NutritionEntryResponse> list(Pageable pageable) {
        return service.list(pageable).map(NutritionEntryMapper::toResponse);
    }

    @GetMapping(params = "athleteId")
    public Page<NutritionEntryResponse> listByAthlete(@RequestParam Long athleteId, Pageable pageable) {
        return service.listByAthlete(athleteId, pageable).map(NutritionEntryMapper::toResponse);
    }

    @GetMapping("/{id}")
    public NutritionEntryResponse get(@PathVariable Long id) {
        return NutritionEntryMapper.toResponse(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NutritionEntryResponse create(@RequestBody @Valid NutritionEntryRequest req) {
        return NutritionEntryMapper.toResponse(service.create(req));
    }

    @PutMapping("/{id}")
    public NutritionEntryResponse update(@PathVariable Long id, @RequestBody @Valid NutritionEntryRequest req) {
        return NutritionEntryMapper.toResponse(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
