package com.eman.tracker.olympicnutritiontracker;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "nutrition_entry")
public class NutritionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate date; // تاريخ الوجبة

    @NotBlank
    private String mealType; // نوع الوجبة: Breakfast, Lunch, Dinner, Snack...

    @Positive
    private Double calories; // السعرات الحرارية

    @ManyToOne
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete; // الرياضي المرتبط بهذا السجل

    public NutritionEntry() {}

    public NutritionEntry(LocalDate date, String mealType, Double calories, Athlete athlete) {
        this.date = date;
        this.mealType = mealType;
        this.calories = calories;
        this.athlete = athlete;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }

    public Athlete getAthlete() { return athlete; }
    public void setAthlete(Athlete athlete) { this.athlete = athlete; }
}
