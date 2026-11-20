package com.eman.tracker.olympicnutritiontracker.repository;

import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NutritionEntryRepository extends JpaRepository<NutritionEntry, Long>, JpaSpecificationExecutor<NutritionEntry> {

    Page<NutritionEntry> findByAthlete_Id(Long athleteId, Pageable pageable);
}
