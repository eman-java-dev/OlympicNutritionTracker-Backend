package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.service.AthleteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AthleteServiceTest {

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private AthleteRepository athleteRepository;

    @Test
    void createAthlete_ok() {
        Athlete athlete = new Athlete();
        athlete.setName("Athlete Test");
        athlete.setAge(25);
        athlete.setGender("F");
        athlete.setHeight(170.0);
        athlete.setWeight(60.0);

        Athlete saved = athleteService.create(athlete);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Athlete Test");
        assertThat(saved.getAge()).isEqualTo(25);
        assertThat(saved.getGender()).isEqualTo("F");
        assertThat(saved.getHeight()).isEqualTo(170.0);
        assertThat(saved.getWeight()).isEqualTo(60.0);
    }

    @Test
    void updateAthlete_ok() {
        Athlete athlete = new Athlete();
        athlete.setName("Old Athlete");
        athlete.setAge(20);
        athlete.setGender("M");
        athlete.setHeight(175.0);
        athlete.setWeight(70.0);

        Athlete saved = athleteService.create(athlete);

        Athlete updateData = new Athlete();
        updateData.setName("Updated Athlete");
        updateData.setAge(30);
        updateData.setGender("M");
        updateData.setHeight(180.0);
        updateData.setWeight(80.0);

        Athlete updated = athleteService.update(saved.getId(), updateData);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getName()).isEqualTo("Updated Athlete");
        assertThat(updated.getAge()).isEqualTo(30);
        assertThat(updated.getGender()).isEqualTo("M");
        assertThat(updated.getHeight()).isEqualTo(180.0);
        assertThat(updated.getWeight()).isEqualTo(80.0);
    }

    @Test
    void deleteAthlete_ok() {
        Athlete athlete = new Athlete();
        athlete.setName("Delete Athlete");
        athlete.setAge(22);
        athlete.setGender("F");
        athlete.setHeight(165.0);
        athlete.setWeight(55.0);

        Athlete saved = athleteService.create(athlete);

        Long id = saved.getId();

        athleteService.delete(id);

        assertThat(athleteRepository.findById(id)).isEmpty();
    }
}