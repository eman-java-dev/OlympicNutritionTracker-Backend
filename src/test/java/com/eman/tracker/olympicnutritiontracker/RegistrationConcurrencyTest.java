package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.dto.RegisterRequest;
import com.eman.tracker.olympicnutritiontracker.repository.UserRepository;
import com.eman.tracker.olympicnutritiontracker.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@BackendIntegrationTest
class RegistrationConcurrencyTest {
    @Autowired AuthenticationService authentication;
    @Autowired UserRepository users;

    @Test
    void overlappingRegistrationsProduceOneAccountAndOneConflict() throws Exception {
        String username = "fixture-" + UUID.randomUUID();
        String password = UUID.randomUUID().toString();
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Callable<Integer> register = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Registration gate timed out");
            try {
                authentication.register(new RegisterRequest(username, password));
                return 200;
            } catch (ResponseStatusException error) {
                return error.getStatusCode().value();
            }
        };

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(register);
            var second = executor.submit(register);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(java.util.List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 409);
            assertThat(users.findByUsername(username)).isPresent();
        } finally {
            users.findByUsername(username).ifPresent(users::delete);
        }
    }
}
