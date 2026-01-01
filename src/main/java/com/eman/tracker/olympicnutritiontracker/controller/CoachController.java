package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.service.CoachService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping
    public Page<Coach> list(@RequestParam(required = false) String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            List<Coach> found = coachService.searchByName(search);
            return new PageImpl<>(found, pageable, found.size());
        }
        return coachService.list(pageable);
    }

    // ✅ GET: Get coach by id
    @GetMapping("/{id}")
    public Coach getById(@PathVariable Long id) {
        return coachService.getById(id);
    }

    // ✅ POST: Create coach
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Coach create(@Valid @RequestBody CoachRequest req) {
        return coachService.create(req.getName(), req.getSpecialty());
    }

    // ✅ PUT: Update coach
    @PutMapping("/{id}")
    public Coach update(@PathVariable Long id, @Valid @RequestBody CoachRequest req) {
        return coachService.update(id, req.getName(), req.getSpecialty());
    }

    // ✅ DELETE: Delete coach
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        coachService.delete(id);
    }

    // ✅ GET: Athletes by coach
    @GetMapping("/{coachId}/athletes")
    public ResponseEntity<List<AthleteResponse>> getAthletesByCoach(@PathVariable Long coachId) {
        List<AthleteResponse> out = coachService.getAthletesByCoach(coachId).stream().map(a -> {
            AthleteResponse r = new AthleteResponse();
            r.setId(a.getId());
            r.setName(a.getName());
            r.setAge(a.getAge());
            r.setGender(a.getGender());
            r.setHeight(a.getHeight());
            r.setWeight(a.getWeight());
            return r;
        }).toList();

        return ResponseEntity.ok(out);
    }

    static class CoachRequest {

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be <= 120 characters")
        private String name;

        @Size(max = 120, message = "specialty must be <= 120 characters")
        private String specialty;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSpecialty() { return specialty; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }
    }

    static class AthleteResponse {
        private Long id;
        private String name;
        private Integer age;
        private String gender;
        private Double height;
        private Double weight;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public Double getHeight() { return height; }
        public void setHeight(Double height) { this.height = height; }

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
    }
}
