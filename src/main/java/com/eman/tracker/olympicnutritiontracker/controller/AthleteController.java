package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.*;
import com.eman.tracker.olympicnutritiontracker.mapper.AthleteMapper;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.service.AthleteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/athletes")
public class AthleteController {

    private final AthleteService service;

    public AthleteController(AthleteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public Page<AthleteResponse> list(Pageable pageable) {
        return service.list(pageable).map(AthleteMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public AthleteResponse get(@PathVariable Long id) {
        return AthleteMapper.toResponse(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AthleteResponse create(@RequestBody AthleteDto dto) {
        Athlete created = service.create(AthleteMapper.toEntity(dto));
        return AthleteMapper.toResponse(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public AthleteResponse update(@PathVariable Long id, @RequestBody AthleteDto dto) {
        Athlete updated = service.update(id, AthleteMapper.toEntity(dto));
        return AthleteMapper.toResponse(updated);
    }

    @PutMapping("/{athleteId}/assign-coach/{coachId}")
    @PreAuthorize("hasRole('ADMIN')")
    public AthleteResponse assignCoach(@PathVariable Long athleteId,
                                       @PathVariable Long coachId) {
        Athlete updated = service.assignCoach(athleteId, coachId);
        return AthleteMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}