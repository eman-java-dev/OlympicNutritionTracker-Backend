package com.eman.tracker.olympicnutritiontracker.repository;

import com.eman.tracker.olympicnutritiontracker.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachRepository extends JpaRepository<Coach, Long> {

    // 🔍 search by name (case-insensitive)
    List<Coach> findByNameContainingIgnoreCase(String name);
}
