package com.eman.tracker.olympicnutritiontracker.dto;

public class AthleteDto {
    private String name;
    private Integer age;
    private String gender;   // "M" / "F" أو نص حر
    private Double height;   // بالسنتيمتر
    private Double weight;   // بالكيلوغرام

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
