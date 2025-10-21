package com.eman.tracker.olympicnutritiontracker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    // للبحث بالاسم
    Page<Athlete> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
