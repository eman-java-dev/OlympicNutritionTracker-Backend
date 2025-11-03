package com.eman.tracker.olympicnutritiontracker.mapper;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryResponse;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;

public final class NutritionEntryMapper {

    private NutritionEntryMapper() {}

    /** من Entity إلى Response */
    public static NutritionEntryResponse toResponse(NutritionEntry e) {
        if (e == null) return null;
        NutritionEntryResponse r = new NutritionEntryResponse();
        r.setId(e.getId());
        r.setDate(e.getDate());
        r.setCalories(e.getCalories());
        r.setProtein(e.getProtein());
        r.setCarbs(e.getCarbs());
        r.setFat(e.getFat());
        r.setNotes(e.getNotes());
        if (e.getAthlete() != null) {
            r.setAthleteId(e.getAthlete().getId());
            r.setAthleteName(e.getAthlete().getName());
        }
        return r;
    }

    /** من Request + Athlete إلى Entity (لإنشاء/تحديث) */
    public static NutritionEntry toEntity(NutritionEntryRequest req, Athlete athlete) {
        NutritionEntry e = new NutritionEntry();
        e.setDate(req.getDate());
        e.setCalories(req.getCalories());
        e.setProtein(req.getProtein());
        e.setCarbs(req.getCarbs());
        e.setFat(req.getFat());
        e.setNotes(req.getNotes());
        e.setAthlete(athlete);
        return e;
    }

    public static void copyForUpdate(NutritionEntryRequest req, NutritionEntry target, Athlete athlete) {
        target.setDate(req.getDate());
        target.setCalories(req.getCalories());
        target.setProtein(req.getProtein());
        target.setCarbs(req.getCarbs());
        target.setFat(req.getFat());
        target.setNotes(req.getNotes());
        target.setAthlete(athlete);
    }
}
