package com.eman.tracker.olympicnutritiontracker;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NutritionEntryRepository extends JpaRepository<NutritionEntry, Long> {
    // جلب كل السجلات حسب ID الرياضي
    List<NutritionEntry> findByAthleteId(Long athleteId);
}
