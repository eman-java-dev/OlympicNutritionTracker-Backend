package com.eman.tracker.olympicnutritiontracker.repository;

import com.eman.tracker.olympicnutritiontracker.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {
}
