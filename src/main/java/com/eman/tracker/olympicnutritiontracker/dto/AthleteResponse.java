package com.eman.tracker.olympicnutritiontracker.dto;

public record AthleteResponse(
        Long id,
        String name,
        Integer age,
        String gender,
        Double height,
        Double weight,
        Double bmi
) {}
