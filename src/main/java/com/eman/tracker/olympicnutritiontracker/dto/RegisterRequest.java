package com.eman.tracker.olympicnutritiontracker.dto;

import com.eman.tracker.olympicnutritiontracker.model.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;
    private String password;
    private Role role;
}
