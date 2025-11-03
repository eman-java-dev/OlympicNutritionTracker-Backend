package com.eman.tracker.olympicnutritiontracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "nutrition_entries")
public class NutritionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fat;

    @Column(length = 300)
    private String notes;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_ref", nullable = false)
    private Athlete athlete;

    @Column(name = "recorded_at", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private java.time.LocalDateTime recordedAt = java.time.LocalDateTime.now();

    // ===== Getters/Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    public Integer getProtein() { return protein; }
    public void setProtein(Integer protein) { this.protein = protein; }
    public Integer getCarbs() { return carbs; }
    public void setCarbs(Integer carbs) { this.carbs = carbs; }
    public Integer getFat() { return fat; }
    public void setFat(Integer fat) { this.fat = fat; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Athlete getAthlete() { return athlete; }
    public void setAthlete(Athlete athlete) { this.athlete = athlete; }
    public java.time.LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(java.time.LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
