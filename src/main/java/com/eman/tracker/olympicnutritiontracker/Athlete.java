package com.eman.tracker.olympicnutritiontracker;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "athlete")
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Min(0) @Max(120)
    private Integer age;

    @NotBlank
    private String gender;     // "M" أو "F"

    @Positive
    private Double height;     // سم

    @Positive
    private Double weight;     // كغ

    private Double bmi;        // يُحسب تلقائيًا في الخدمة

    // --- لازم موجود لـ JPA
    public Athlete() {}

    // اختياري: مُنشئ مساعد
    public Athlete(String name, Integer age, String gender, Double height, Double weight, Double bmi) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.bmi = bmi;
    }

    // --- Getters & Setters
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

    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
}

