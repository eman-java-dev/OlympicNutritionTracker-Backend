package com.eman.tracker.olympicnutritiontracker.controller;

// نفس imports الموجودة عندك
import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.service.CoachService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public Page<Coach> list(@RequestParam(required = false) String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            List<Coach> found = coachService.searchByName(search);
            return new PageImpl<>(found, pageable, found.size());
        }
        return coachService.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public Coach getById(@PathVariable Long id) {
        return coachService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Coach create(@Valid @RequestBody CoachRequest req) {
        return coachService.create(req.getName(), req.getSpecialty(), req.getEmail(), req.getPhone());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Coach update(@PathVariable Long id, @Valid @RequestBody CoachRequest req) {
        return coachService.update(id, req.getName(), req.getSpecialty(), req.getEmail(), req.getPhone());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        coachService.delete(id);
    }

    @GetMapping("/{coachId}/athletes")
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<List<AthleteResponse>> getAthletesByCoach(@PathVariable Long coachId) {
        List<AthleteResponse> out = coachService.getAthletesByCoach(coachId)
                .stream()
                .map(a -> {
                    AthleteResponse r = new AthleteResponse();
                    r.setId(a.getId());
                    r.setName(a.getName());
                    r.setAge(a.getAge());
                    r.setGender(a.getGender());
                    r.setHeight(a.getHeight());
                    r.setWeight(a.getWeight());
                    return r;
                })
                .toList();

        return ResponseEntity.ok(out);
    }

    static class CoachRequest {
        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be <= 120 characters")
        private String name;

        @Size(max = 120, message = "specialty must be <= 120 characters")
        private String specialty;

        @Email(message = "email must be valid")
        @Size(max = 160, message = "email must be <= 160 characters")
        private String email;

        @Size(max = 40, message = "phone must be <= 40 characters")
        private String phone;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSpecialty() { return specialty; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
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