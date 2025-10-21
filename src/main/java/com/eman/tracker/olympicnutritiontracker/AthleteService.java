package com.eman.tracker.olympicnutritiontracker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AthleteService {

    private final AthleteRepository repo;

    public AthleteService(AthleteRepository repo) {
        this.repo = repo;
    }

    // عرض كل الرياضيين مع إمكانية البحث والصفحات
    public Page<Athlete> list(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByNameContainingIgnoreCase(q.trim(), pageable);
    }

    // جلب رياضي واحد حسب الـ ID
    public Athlete get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete id=" + id + " not found"));
    }

    // إنشاء رياضي جديد
    public Athlete create(Athlete a) {
        if (a.getHeight() != null && a.getWeight() != null) {
            a.setBmi(calculateBmi(a.getHeight(), a.getWeight()));
        }
        return repo.save(a);
    }
    // تحديث بيانات الرياضي
    public Athlete update(Long id, Athlete incoming) {
        Athlete existing = get(id);
        if (incoming.getName() != null) existing.setName(incoming.getName());
        if (incoming.getAge() != null) existing.setAge(incoming.getAge());
        if (incoming.getGender() != null) existing.setGender(incoming.getGender());
        if (incoming.getHeight() != null) existing.setHeight(incoming.getHeight());
        if (incoming.getWeight() != null) existing.setWeight(incoming.getWeight());

        if (existing.getHeight() != null && existing.getWeight() != null) {
            existing.setBmi(calculateBmi(existing.getHeight(), existing.getWeight()));
        }
        return repo.save(existing);
    }

    // حذف رياضي
    public void delete(Long id) {
        repo.delete(get(id));
    }

    // دالة حساب BMI تلقائيًا
    private Double calculateBmi(Double heightCm, Double weightKg) {
        double h = heightCm / 100.0;
        return Math.round((weightKg / (h * h)) * 10.0) / 10.0;
    }
}

