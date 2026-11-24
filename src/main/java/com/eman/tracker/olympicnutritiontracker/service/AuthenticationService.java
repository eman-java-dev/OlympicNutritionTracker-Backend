package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.config.JwtService;
import com.eman.tracker.olympicnutritiontracker.dto.AuthenticationResponse;
import com.eman.tracker.olympicnutritiontracker.dto.LoginRequest;
import com.eman.tracker.olympicnutritiontracker.dto.RegisterRequest;
import com.eman.tracker.olympicnutritiontracker.model.Role;
import com.eman.tracker.olympicnutritiontracker.model.User;
import com.eman.tracker.olympicnutritiontracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthenticationResponse register(RegisterRequest request) {
        if (exceedsBcryptLimit(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
        String password = request.getPassword();
        if (!StandardCharsets.UTF_8.newEncoder().canEncode(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be valid Unicode");
        }
        if (password.codePointCount(0, password.length()) < 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must contain at least 15 characters");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw duplicateUsername();
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ATHLETE)
                .build();
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw duplicateUsername();
            }
            throw ex;
        }
        return responseFor(customUserDetailsService.loadUserByUsername(user.getUsername()));
    }

    public AuthenticationResponse login(LoginRequest request) {
        if (exceedsBcryptLimit(request.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword()));
        return responseFor((UserDetails) authentication.getPrincipal());
    }

    private AuthenticationResponse responseFor(UserDetails user) {
        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no role"));
        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(user))
                .username(user.getUsername())
                .role(role)
                .build();
    }

    private static boolean exceedsBcryptLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES;
    }

    private static ResponseStatusException duplicateUsername() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }
}
