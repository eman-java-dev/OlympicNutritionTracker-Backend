package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.service.CoachService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // جميع الأدوار يمكنها عرض قائمة الكوتشات
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public Page<Coach> list(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        if (search != null && !search.isBlank()) {
            List<Coach> result = coachService.searchByName(search);

            return new PageImpl<>(
                    result,
                    pageable,
                    result.size()
            );
        }

        return coachService.list(pageable);
    }

    // جميع الأدوار يمكنها عرض Coach واحد
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public Coach getById(@PathVariable Long id) {
        return coachService.getById(id);
    }

    // الإدمن فقط يمكنه إنشاء Coach
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Coach create(@Valid @RequestBody CoachRequest request) {
        return coachService.create(
                request.getName(),
                request.getSpecialty(),
                request.getEmail(),
                request.getPhone()
        );
    }

    // الإدمن فقط يمكنه تعديل Coach
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Coach update(
            @PathVariable Long id,
            @Valid @RequestBody CoachRequest request
    ) {
        return coachService.update(
                id,
                request.getName(),
                request.getSpecialty(),
                request.getEmail(),
                request.getPhone()
        );
    }

    // الإدمن فقط يمكنه حذف Coach
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        coachService.delete(id);
    }

    // جميع الأدوار يمكنها رؤية الرياضيين المرتبطين بـ Coach
    @GetMapping("/{coachId}/athletes")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public ResponseEntity<List<AthleteResponse>> getAthletesByCoach(
            @PathVariable Long coachId
    ) {
        List<AthleteResponse> response = coachService
                .getAthletesByCoach(coachId)
                .stream()
                .map(athlete -> {
                    AthleteResponse dto = new AthleteResponse();

                    dto.setId(athlete.getId());
                    dto.setName(athlete.getName());
                    dto.setAge(athlete.getAge());
                    dto.setGender(athlete.getGender());
                    dto.setHeight(athlete.getHeight());
                    dto.setWeight(athlete.getWeight());

                    return dto;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    public static class CoachRequest {

        @NotBlank(message = "Le nom est obligatoire")
        @Size(
                max = 120,
                message = "Le nom ne doit pas dépasser 120 caractères"
        )
        private String name;

        @Size(
                max = 120,
                message = "La spécialité ne doit pas dépasser 120 caractères"
        )
        private String specialty;

        @Email(message = "L'adresse e-mail n'est pas valide")
        @Size(
                max = 160,
                message = "L'adresse e-mail ne doit pas dépasser 160 caractères"
        )
        private String email;

        @Size(
                max = 40,
                message = "Le numéro de téléphone ne doit pas dépasser 40 caractères"
        )
        private String phone;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSpecialty() {
            return specialty;
        }

        public void setSpecialty(String specialty) {
            this.specialty = specialty;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class AthleteResponse {

        private Long id;
        private String name;
        private Integer age;
        private String gender;
        private Double height;
        private Double weight;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Double getHeight() {
            return height;
        }

        public void setHeight(Double height) {
            this.height = height;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }
    }
}