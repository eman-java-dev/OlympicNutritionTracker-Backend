package com.eman.tracker.olympicnutritiontracker;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionEntryController {

    private final NutritionEntryService service;

    public NutritionEntryController(NutritionEntryService service) {
        this.service = service;
    }

    // عرض كل السجلات
    @GetMapping
    public List<NutritionEntry> listAll() {
        return service.getAll();
    }

    // عرض السجلات الخاصة برياضي معين
    @GetMapping("/athlete/{athleteId}")
    public List<NutritionEntry> listByAthlete(@PathVariable Long athleteId) {
        return service.getByAthlete(athleteId);
    }

    // عرض سجل واحد
    @GetMapping("/{id}")
    public NutritionEntry get(@PathVariable Long id) {
        return service.get(id);
    }

    // إضافة سجل جديد لرياضي
    @PostMapping("/athlete/{athleteId}")
    public NutritionEntry create(@PathVariable Long athleteId, @RequestBody NutritionEntry entry) {
        return service.create(athleteId, entry);
    }

    // تحديث سجل
    @PutMapping("/{id}")
    public NutritionEntry update(@PathVariable Long id, @RequestBody NutritionEntry entry) {
        return service.update(id, entry);
    }

    // حذف سجل
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

