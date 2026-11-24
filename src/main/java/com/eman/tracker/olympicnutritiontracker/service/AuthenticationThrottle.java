package com.eman.tracker.olympicnutritiontracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class AuthenticationThrottle {

    public enum Operation { LOGIN, REGISTER }

    private record Key(Operation operation, String account) {}
    private record Window(Instant expiresAt, int used) {}

    private final Clock clock;
    private final Duration duration;
    private final int capacity;
    private final int loginLimit;
    private final int registrationLimit;
    private final Map<Key, Window> accounts = new LinkedHashMap<>();
    private final Map<Operation, Window> overflow = new java.util.EnumMap<>(Operation.class);

    @Autowired
    public AuthenticationThrottle(
            @Value("${app.auth.throttle.window:1m}") Duration duration,
            @Value("${app.auth.throttle.capacity:10000}") int capacity,
            @Value("${app.auth.throttle.login-limit:10}") int loginLimit,
            @Value("${app.auth.throttle.registration-limit:5}") int registrationLimit) {
        this(Clock.systemUTC(), duration, capacity, loginLimit, registrationLimit);
    }

    AuthenticationThrottle(Clock clock, Duration duration, int capacity, int loginLimit, int registrationLimit) {
        if (duration.isNegative() || duration.isZero() || capacity < 1 || loginLimit < 1 || registrationLimit < 1) {
            throw new IllegalArgumentException("Authentication throttle settings must be positive");
        }
        this.clock = clock;
        this.duration = duration;
        this.capacity = capacity;
        this.loginLimit = loginLimit;
        this.registrationLimit = registrationLimit;
    }

    public synchronized void check(Operation operation, String username) {
        Instant now = clock.instant();
        var keys = accounts.values().iterator();
        while (keys.hasNext() && !keys.next().expiresAt().isAfter(now)) {
            keys.remove();
        }

        String account = Normalizer.normalize(username.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
        Key key = new Key(operation, account);
        int limit = operation == Operation.LOGIN ? loginLimit : registrationLimit;
        Window current = accounts.get(key);
        if (current == null && accounts.size() >= capacity) {
            overflow.put(operation, consume(overflow.get(operation), now, limit));
        } else {
            accounts.put(key, consume(current, now, limit));
        }
    }

    private Window consume(Window window, Instant now, int limit) {
        if (window == null || !window.expiresAt().isAfter(now)) {
            return new Window(now.plus(duration), 1);
        }
        if (window.used() >= limit) {
            Duration remaining = Duration.between(now, window.expiresAt());
            long seconds = remaining.toSeconds() + (remaining.getNano() == 0 ? 0 : 1);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many authentication attempts") {
                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set(HttpHeaders.RETRY_AFTER, Long.toString(seconds));
                    return headers;
                }
            };
        }
        return new Window(window.expiresAt(), window.used() + 1);
    }
}
