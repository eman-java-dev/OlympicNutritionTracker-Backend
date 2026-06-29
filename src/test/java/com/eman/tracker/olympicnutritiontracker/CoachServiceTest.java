package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.repository.CoachRepository;
import com.eman.tracker.olympicnutritiontracker.service.CoachService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CoachServiceTest {

    @Autowired
    private CoachService coachService;

    @Autowired
    private CoachRepository coachRepository;

    @Test
    void createCoach_ok() {
        Coach saved = coachService.create(
                "Coach Test",
                "Nutrition",
                "coach@test.com",
                "0600000000"
        );

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Coach Test");
        assertThat(saved.getSpecialty()).isEqualTo("Nutrition");
        assertThat(saved.getEmail()).isEqualTo("coach@test.com");
        assertThat(saved.getPhone()).isEqualTo("0600000000");
    }

    @Test
    void updateCoach_ok() {
        Coach saved = coachService.create(
                "Old Coach",
                "Old Specialty",
                "old@test.com",
                "0611111111"
        );

        Coach updated = coachService.update(
                saved.getId(),
                "Updated Coach",
                "Performance",
                "updated@test.com",
                "0622222222"
        );

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getName()).isEqualTo("Updated Coach");
        assertThat(updated.getSpecialty()).isEqualTo("Performance");
        assertThat(updated.getEmail()).isEqualTo("updated@test.com");
        assertThat(updated.getPhone()).isEqualTo("0622222222");
    }

    @Test
    void deleteCoach_ok() {
        Coach saved = coachService.create(
                "Delete Coach",
                "Fitness",
                "delete@test.com",
                "0633333333"
        );

        Long id = saved.getId();

        coachService.delete(id);

        assertThat(coachRepository.findById(id)).isEmpty();
    }
}