package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.ConsultationRequest;
import com.eman.tracker.olympicnutritiontracker.dto.ConsultationResponse;
import com.eman.tracker.olympicnutritiontracker.mapper.ConsultationMapper;
import com.eman.tracker.olympicnutritiontracker.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService service;

    public ConsultationController(ConsultationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public Page<ConsultationResponse> list(Pageable pageable) {
        return service.list(pageable).map(ConsultationMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public ConsultationResponse get(@PathVariable Long id) {
        return ConsultationMapper.toResponse(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultationResponse create(@Valid @RequestBody ConsultationRequest req) {
        return ConsultationMapper.toResponse(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ConsultationResponse update(@PathVariable Long id,
                                       @Valid @RequestBody ConsultationRequest req) {
        return ConsultationMapper.toResponse(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}