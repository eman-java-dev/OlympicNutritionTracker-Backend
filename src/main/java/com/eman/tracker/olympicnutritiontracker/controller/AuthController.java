package com.eman.tracker.olympicnutritiontracker.controller;

import com.eman.tracker.olympicnutritiontracker.dto.AuthenticationResponse;
import com.eman.tracker.olympicnutritiontracker.dto.LoginRequest;
import com.eman.tracker.olympicnutritiontracker.dto.RegisterRequest;
import com.eman.tracker.olympicnutritiontracker.service.AuthenticationService;
import com.eman.tracker.olympicnutritiontracker.service.AuthenticationThrottle;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final AuthenticationThrottle throttle;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        throttle.check(AuthenticationThrottle.Operation.REGISTER, request.getUsername());
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        throttle.check(AuthenticationThrottle.Operation.LOGIN, request.getUsername());
        return ResponseEntity.ok(authenticationService.login(request));
    }
}