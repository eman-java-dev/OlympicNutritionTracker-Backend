package com.eman.tracker.olympicnutritiontracker.dto;

import jakarta.validation.constraints.*;

public record AthleteDto(
        @NotBlank(message = "name is required") String name,
        @NotNull @Min(0) @Max(120) Integer age,
        @NotBlank String gender,          // "M" أو "F"
        @NotNull @Positive Double height, // بالسنتمتر
        @NotNull @Positive Double weight  // بالكيلوغرام
) {}
