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
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must contain between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(max = 72, message = "Password must be at most 72 UTF-8 bytes")
    private String password;
}