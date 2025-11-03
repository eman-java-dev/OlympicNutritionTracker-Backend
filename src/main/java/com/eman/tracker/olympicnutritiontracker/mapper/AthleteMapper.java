package com.eman.tracker.olympicnutritiontracker.mapper;

import com.eman.tracker.olympicnutritiontracker.dto.AthleteDto;
import com.eman.tracker.olympicnutritiontracker.dto.AthleteResponse;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;

public final class AthleteMapper {
    private AthleteMapper() {}

    public static Athlete toEntity(AthleteDto dto) {
        if (dto == null) return null;
        Athlete a = new Athlete();
        a.setName(dto.getName());
        a.setAge(dto.getAge());
        a.setGender(dto.getGender());
        a.setHeight(dto.getHeight());
        a.setWeight(dto.getWeight());
        return a;
    }

    public static AthleteResponse toResponse(Athlete a) {
        if (a == null) return null;
        Double bmi = null;
        if (a.getHeight() != null && a.getWeight() != null && a.getHeight() > 0) {
            double hM = a.getHeight() / 100.0;
            bmi = a.getWeight() / (hM * hM);
        }
        return new AthleteResponse(
                a.getId(),
                a.getName(),
                a.getAge(),
                a.getGender(),
                a.getHeight(),
                a.getWeight(),
                bmi
        );
    }
}
