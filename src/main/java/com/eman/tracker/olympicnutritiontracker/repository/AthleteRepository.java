package com.eman.tracker.olympicnutritiontracker.repository;

import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {
}
