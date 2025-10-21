package com.eman.tracker.olympicnutritiontracker.mapper;

import com.eman.tracker.olympicnutritiontracker.Athlete;
import com.eman.tracker.olympicnutritiontracker.dto.AthleteDto;
import com.eman.tracker.olympicnutritiontracker.dto.AthleteResponse;

public class AthleteMapper {

    public static Athlete toEntity(AthleteDto dto) {
        Athlete a = new Athlete();
        a.setName(dto.name());
        a.setAge(dto.age());
        a.setGender(dto.gender());
        a.setHeight(dto.height());
        a.setWeight(dto.weight());
        return a;
    }

    public static AthleteResponse toResponse(Athlete a) {
        return new AthleteResponse(
                a.getId(),
                a.getName(),
                a.getAge(),
                a.getGender(),
                a.getHeight(),
                a.getWeight(),
                a.getBmi()
        );
    }
}

