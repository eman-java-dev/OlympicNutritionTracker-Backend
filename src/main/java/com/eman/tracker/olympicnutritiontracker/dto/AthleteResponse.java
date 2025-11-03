package com.eman.tracker.olympicnutritiontracker.dto;

public class AthleteResponse {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private Double height;
    private Double weight;
    private Double bmi; // Nullable

    public AthleteResponse() {}

    public AthleteResponse(Long id, String name, Integer age, String gender,
                           Double height, Double weight, Double bmi) {
        this.id = id; this.name = name; this.age = age; this.gender = gender;
        this.height = height; this.weight = weight; this.bmi = bmi;
    }

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
