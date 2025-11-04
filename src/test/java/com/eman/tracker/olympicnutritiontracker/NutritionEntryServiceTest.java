package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.dto.NutritionEntryRequest;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
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
    NutritionEntryService service;
    @Autowired AthleteRepository athletes;

    @Test
    void createNutritionEntry_ok() {
        Athlete a = new Athlete();
        a.setName("Test A");
        a.setAge(22);
        a.setGender("F");
        a.setHeight(170.0);
        a.setWeight(60.0);
        a = athletes.save(a);

        NutritionEntryRequest req = new NutritionEntryRequest();
        req.setAthleteId(a.getId());
        req.setDate(LocalDate.now());
        req.setCalories(1800);
        req.setProtein(120);
        req.setCarbs(200);
        req.setFat(50);
        req.setNotes("unit-test");

        var res = service.create(req);
        assertThat(res.getId()).isNotNull();
        assertThat(res.getAthleteName()).isEqualTo("Test A");
    }
}
