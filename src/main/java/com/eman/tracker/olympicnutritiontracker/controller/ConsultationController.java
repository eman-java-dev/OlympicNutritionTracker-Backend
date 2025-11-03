package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.ConsultationRequest;
import com.eman.tracker.olympicnutritiontracker.dto.ConsultationResponse;
import com.eman.tracker.olympicnutritiontracker.mapper.ConsultationMapper;
import com.eman.tracker.olympicnutritiontracker.service.ConsultationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService service;

    public ConsultationController(ConsultationService service) {
        this.service = service;
    }

    // GET: list all consultations (paged)
    @GetMapping
    public Page<ConsultationResponse> list(Pageable pageable) {
        return service.list(pageable).map(ConsultationMapper::toResponse);
    }

    // POST: create new consultation
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultationResponse create(@RequestBody ConsultationRequest req) {
        return ConsultationMapper.toResponse(
                service.create(req.getMessage(),
                        req.getScheduledAt(),
                        req.getAthleteId(),
                        req.getCoachId())
        );
    }

    // PUT: update consultation by id
    @PutMapping("/{id}")
    public ConsultationResponse update(@PathVariable Long id,
                                       @RequestBody ConsultationRequest req) {
        return ConsultationMapper.toResponse(
                service.update(id,
                        req.getMessage(),
                        req.getScheduledAt(),
                        req.getAthleteId(),
                        req.getCoachId())
        );
    }

    // DELETE: delete consultation by id
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
