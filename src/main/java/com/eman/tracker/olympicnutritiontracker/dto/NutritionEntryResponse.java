package com.eman.tracker.olympicnutritiontracker.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
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
}
