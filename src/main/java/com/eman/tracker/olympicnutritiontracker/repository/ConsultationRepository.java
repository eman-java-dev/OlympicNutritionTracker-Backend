package com.eman.tracker.olympicnutritiontracker.repository;

import com.eman.tracker.olympicnutritiontracker.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByCoach_Id(Long coachId);

    void deleteByCoach_Id(Long coachId);
}