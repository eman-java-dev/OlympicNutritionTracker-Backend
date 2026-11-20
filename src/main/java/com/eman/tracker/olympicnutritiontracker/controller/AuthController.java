package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.AuthenticationResponse;
import com.eman.tracker.olympicnutritiontracker.dto.LoginRequest;
import com.eman.tracker.olympicnutritiontracker.dto.RegisterRequest;
import com.eman.tracker.olympicnutritiontracker.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}