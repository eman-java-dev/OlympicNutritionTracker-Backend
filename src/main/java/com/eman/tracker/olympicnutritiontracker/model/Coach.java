package com.eman.tracker.olympicnutritiontracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "coaches")
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String specialty;

    // ✅ نخفي العلاقات من JSON (باش الـAPI يرجّع Coach نظيف)
    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<Consultation> consultations;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<Athlete> athletes;

    // ===== Getters/Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public List<Consultation> getConsultations() { return consultations; }
    public void setConsultations(List<Consultation> consultations) { this.consultations = consultations; }

    public List<Athlete> getAthletes() { return athletes; }
    public void setAthletes(List<Athlete> athletes) { this.athletes = athletes; }
}
