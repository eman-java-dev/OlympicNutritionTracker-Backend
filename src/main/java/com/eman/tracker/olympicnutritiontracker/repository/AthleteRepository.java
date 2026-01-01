package com.eman.tracker.olympicnutritiontracker.repository;

import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {

    // 🟢 دالة جديدة: تجيب كل الرياضيين حسب رقم المدرب
    List<Athlete> findByCoach_Id(Long coachId);
}
