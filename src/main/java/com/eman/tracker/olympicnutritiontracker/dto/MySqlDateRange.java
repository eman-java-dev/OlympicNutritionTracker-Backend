package com.eman.tracker.olympicnutritiontracker.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MySqlDateRange.Validator.class)
public @interface MySqlDateRange {
    String message() default "Date must have a year between 1000 and 9999";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<MySqlDateRange, TemporalAccessor> {
        @Override
        public boolean isValid(TemporalAccessor value, ConstraintValidatorContext context) {
            return value == null || (value.isSupported(ChronoField.YEAR)
                    && value.get(ChronoField.YEAR) >= 1000 && value.get(ChronoField.YEAR) <= 9999);
        }
    }
}
