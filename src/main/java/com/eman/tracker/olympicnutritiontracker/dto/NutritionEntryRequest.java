package com.eman.tracker.olympicnutritiontracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class NutritionEntryRequest {
    @NotNull
    @MySqlDateRange
    private LocalDate date;     // yyyy-MM-dd
    @PositiveOrZero
    private Integer calories;
    @PositiveOrZero
    private Integer protein;
    @PositiveOrZero
    private Integer carbs;
    @PositiveOrZero
    private Integer fat;
    @Size(max = 300)
    private String notes;
    @NotNull
    @Positive
    private Long athleteId;

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
}
