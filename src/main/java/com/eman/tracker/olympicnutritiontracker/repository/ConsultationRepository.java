package com.eman.tracker.olympicnutritiontracker.repository;

import com.eman.tracker.olympicnutritiontracker.model.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Page<Consultation> findByCoach_Id(Long coachId, Pageable pageable);
    Page<Consultation> findByAthlete_Id(Long athleteId, Pageable pageable);
}
