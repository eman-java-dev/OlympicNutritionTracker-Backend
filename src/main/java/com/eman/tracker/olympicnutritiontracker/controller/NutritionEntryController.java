package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryResponse;
import com.eman.tracker.olympicnutritiontracker.mapper.NutritionEntryMapper;
import com.eman.tracker.olympicnutritiontracker.service.NutritionEntryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nutrition-entries")
public class NutritionEntryController {

    private final NutritionEntryService service;

    public NutritionEntryController(NutritionEntryService service) {
        this.service = service;
    }

    // جميع الأدوار يمكنها عرض البيانات الغذائية
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public Page<NutritionEntryResponse> list(Pageable pageable) {
        return service.list(pageable)
                .map(NutritionEntryMapper::toResponse);
    }

    // عرض البيانات الخاصة برياضي معين
    @GetMapping(params = "athleteId")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public Page<NutritionEntryResponse> listByAthlete(
            @RequestParam Long athleteId,
            Pageable pageable
    ) {
        return service.listByAthlete(athleteId, pageable)
                .map(NutritionEntryMapper::toResponse);
    }

    // جميع الأدوار يمكنها عرض سجل واحد
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','ATHLETE')")
    public NutritionEntryResponse get(@PathVariable Long id) {
        return NutritionEntryMapper.toResponse(service.get(id));
    }

    // الإدمن والكوتش يمكنهما الإضافة
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @ResponseStatus(HttpStatus.CREATED)
    public NutritionEntryResponse create(
            @Valid @RequestBody NutritionEntryRequest request
    ) {
        return NutritionEntryMapper.toResponse(
                service.create(request)
        );
    }

    // الإدمن والكوتش يمكنهما التعديل
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public NutritionEntryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody NutritionEntryRequest request
    ) {
        return NutritionEntryMapper.toResponse(
                service.update(id, request)
        );
    }

    // الإدمن فقط يمكنه الحذف
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}