package com.eman.tracker.olympicnutritiontracker.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AthleteDto {
    @NotBlank
    @Size(max = 120)
    private String name;
    @Positive
    private Integer age;
    @Size(max = 10)
    private String gender;   // "M" / "F" أو نص حر
    @PositiveMeasurement
    private Double height;   // بالسنتيمتر
    @PositiveMeasurement
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

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PositiveMeasurementValidator.class)
    public @interface PositiveMeasurement {
        String message() default "Must be a finite number greater than zero";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public static class PositiveMeasurementValidator implements ConstraintValidator<PositiveMeasurement, Double> {
        @Override
        public boolean isValid(Double value, ConstraintValidatorContext context) {
            return value == null || (Double.isFinite(value) && value > 0);
        }
    }
}
