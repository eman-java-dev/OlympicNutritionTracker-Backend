package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;
import com.eman.tracker.olympicnutritiontracker.repository.NutritionEntryRepository;
import com.eman.tracker.olympicnutritiontracker.service.AthleteService;
import com.eman.tracker.olympicnutritiontracker.service.NutritionEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NutritionEntryServiceTest {

    @Autowired
    private NutritionEntryService nutritionEntryService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private NutritionEntryRepository nutritionEntryRepository;

    @Test
    void createNutritionEntry_ok() {
        Athlete athlete = new Athlete();
        athlete.setName("Test Athlete");
        athlete.setAge(25);
        athlete.setGender("M");
        athlete.setHeight(180.0);
        athlete.setWeight(75.0);

        athlete = athleteService.create(athlete);

        NutritionEntryRequest request = new NutritionEntryRequest();
        request.setAthleteId(athlete.getId());
        request.setDate(LocalDate.now());
        request.setCalories(2500);
        request.setProtein(180);
        request.setCarbs(250);
        request.setFat(70);
        request.setNotes("JUnit Test");

        NutritionEntry saved = nutritionEntryService.create(request);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCalories()).isEqualTo(2500);
        assertThat(saved.getProtein()).isEqualTo(180);
        assertThat(saved.getCarbs()).isEqualTo(250);
        assertThat(saved.getFat()).isEqualTo(70);
        assertThat(saved.getNotes()).isEqualTo("JUnit Test");
        assertThat(saved.getAthlete().getId()).isEqualTo(athlete.getId());
    }

    @Test
    void updateNutritionEntry_ok() {
        Athlete athlete = new Athlete();
        athlete.setName("Athlete Update");
        athlete.setAge(22);
        athlete.setGender("F");
        athlete.setHeight(170.0);
        athlete.setWeight(60.0);

        athlete = athleteService.create(athlete);

        NutritionEntryRequest request = new NutritionEntryRequest();
        request.setAthleteId(athlete.getId());
        request.setDate(LocalDate.now());
        request.setCalories(2000);
        request.setProtein(100);
        request.setCarbs(200);
        request.setFat(60);
        request.setNotes("Before");

        NutritionEntry saved = nutritionEntryService.create(request);

        NutritionEntryRequest update = new NutritionEntryRequest();
        update.setAthleteId(athlete.getId());
        update.setDate(LocalDate.now());
        update.setCalories(2800);
        update.setProtein(220);
        update.setCarbs(300);
        update.setFat(80);
        update.setNotes("Updated");

        NutritionEntry updated = nutritionEntryService.update(saved.getId(), update);

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getCalories()).isEqualTo(2800);
        assertThat(updated.getProtein()).isEqualTo(220);
        assertThat(updated.getCarbs()).isEqualTo(300);
        assertThat(updated.getFat()).isEqualTo(80);
        assertThat(updated.getNotes()).isEqualTo("Updated");
    }

    @Test
    void deleteNutritionEntry_ok() {
        Athlete athlete = new Athlete();
        athlete.setName("Delete Athlete");
        athlete.setAge(20);
        athlete.setGender("M");
        athlete.setHeight(175.0);
        athlete.setWeight(70.0);

        athlete = athleteService.create(athlete);

        NutritionEntryRequest request = new NutritionEntryRequest();
        request.setAthleteId(athlete.getId());
        request.setDate(LocalDate.now());
        request.setCalories(2200);
        request.setProtein(150);
        request.setCarbs(220);
        request.setFat(65);
        request.setNotes("Delete");

        NutritionEntry saved = nutritionEntryService.create(request);

        Long id = saved.getId();

        nutritionEntryService.delete(id);

        assertThat(nutritionEntryRepository.findById(id)).isEmpty();
    }
}