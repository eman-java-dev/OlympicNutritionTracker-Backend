package com.eman.tracker.olympicnutritiontracker.mapper;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryResponse;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;

public final class NutritionEntryMapper {

    private NutritionEntryMapper() {}

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

    public static NutritionEntryResponse toResponse(NutritionEntry e) {
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

    public static void copyForUpdate(NutritionEntryRequest req, NutritionEntry existing, Athlete athlete) {
        existing.setDate(req.getDate());
        existing.setCalories(req.getCalories());
        existing.setProtein(req.getProtein());
        existing.setCarbs(req.getCarbs());
        existing.setFat(req.getFat());
        existing.setNotes(req.getNotes());
        existing.setAthlete(athlete);
    }
}
