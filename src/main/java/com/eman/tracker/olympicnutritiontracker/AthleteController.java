package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.dto.AthleteDto;
import com.eman.tracker.olympicnutritiontracker.dto.AthleteResponse;
import com.eman.tracker.olympicnutritiontracker.mapper.AthleteMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/athletes")
public class AthleteController {

    private final AthleteService service;

    public AthleteController(AthleteService service) {
        this.service = service;
    }

    // GET /api/athletes?q=eman&page=0&size=10
    @GetMapping
    public Page<AthleteResponse> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.list(q, pageable).map(AthleteMapper::toResponse);
    }

    // GET /api/athletes/{id}
    @GetMapping("/{id}")
    public AthleteResponse get(@PathVariable Long id) {
        return AthleteMapper.toResponse(service.get(id));
    }

    // POST /api/athletes
    @PostMapping
    public ResponseEntity<AthleteResponse> create(@Valid @RequestBody AthleteDto dto) {
        Athlete created = service.create(AthleteMapper.toEntity(dto));
        return new ResponseEntity<>(AthleteMapper.toResponse(created), HttpStatus.CREATED);
    }

    // PUT /api/athletes/{id}
    @PutMapping("/{id}")
    public AthleteResponse update(@PathVariable Long id, @Valid @RequestBody AthleteDto dto) {
        Athlete updated = service.update(id, AthleteMapper.toEntity(dto));
        return AthleteMapper.toResponse(updated);
    }

    // DELETE /api/athletes/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
