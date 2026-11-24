package com.eman.tracker.olympicnutritiontracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @NotBlank
    // A MySQL VARCHAR(255) username can use up to 510 UTF-16 code units.
    @Size(max = 510)
    private String username;
    @NotBlank
    private String password;
}
