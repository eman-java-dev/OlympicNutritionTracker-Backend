package com.eman.tracker.olympicnutritiontracker.dto;

import java.time.LocalDate;

public class NutritionEntryResponse {
    private Long id;
    private LocalDate date;
    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fat;
    private String notes;

    private Long athleteId;
    private String athleteName;

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

    public Long getAthleteId() { return athleteId; }
    public void setAthleteId(Long athleteId) { this.athleteId = athleteId; }

    public String getAthleteName() { return athleteName; }
    public void setAthleteName(String athleteName) { this.athleteName = athleteName; }
}
