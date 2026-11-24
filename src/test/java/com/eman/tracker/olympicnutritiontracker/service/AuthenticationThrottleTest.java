package com.eman.tracker.olympicnutritiontracker.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static com.eman.tracker.olympicnutritiontracker.service.AuthenticationThrottle.Operation.LOGIN;
import static com.eman.tracker.olympicnutritiontracker.service.AuthenticationThrottle.Operation.REGISTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationThrottleTest {
    @Test
    void keepsSeparateOperationAndAccountWindowsAndRecoversAtExpiry() {
        Clock clock = mock(Clock.class);
        Instant start = Instant.EPOCH;
        when(clock.instant()).thenReturn(start);
        var throttle = new AuthenticationThrottle(clock, Duration.ofSeconds(20), 3, 2, 1);
        throttle.check(LOGIN, " Renée ");
        throttle.check(LOGIN, "renee");
        when(clock.instant()).thenReturn(start.plusMillis(18001));
        var limited = assertThrows(ResponseStatusException.class, () -> throttle.check(LOGIN, "RENEE"));
        assertThat(limited.getStatusCode().value()).isEqualTo(429);
        assertThat(limited.getHeaders().getFirst("Retry-After")).isEqualTo("2");
        throttle.check(REGISTER, "renee");
        throttle.check(LOGIN, "another");
        when(clock.instant()).thenReturn(start.plusSeconds(20));
        throttle.check(LOGIN, "renee");
    }

    @Test
    void retainsTrackedWindowsWhenCapacityIsReached() {
        var throttle = new AuthenticationThrottle(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                Duration.ofMinutes(1), 1, 1, 1);
        throttle.check(LOGIN, "tracked");
        throttle.check(LOGIN, "overflow");
        assertThrows(ResponseStatusException.class, () -> throttle.check(LOGIN, "another-overflow"));
        assertThrows(ResponseStatusException.class, () -> throttle.check(LOGIN, "tracked"));
    }

    @Test
    void countsConcurrentRequestsAtomically() throws Exception {
        var throttle = new AuthenticationThrottle(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                Duration.ofMinutes(1), 10, 3, 1);
        var start = new CountDownLatch(1);
        var passed = new AtomicInteger();
        try (var pool = Executors.newFixedThreadPool(8)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<Void>>();
            for (int i = 0; i < 12; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    try {
                        throttle.check(LOGIN, "same-account");
                        passed.incrementAndGet();
                    } catch (ResponseStatusException error) {
                        assertThat(error.getStatusCode().value()).isEqualTo(429);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (var future : futures) {
                future.get(5, java.util.concurrent.TimeUnit.SECONDS);
            }
        }
        assertThat(passed).hasValue(3);
    }
}
