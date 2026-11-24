package com.eman.tracker.olympicnutritiontracker.config;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    @Test
    void rejectsObsoleteKeyAfterRotation() {
        var old = new JwtService(randomKey(), 60_000);
        var replacement = new JwtService(randomKey(), 60_000);
        var user = User.withUsername("synthetic-account").password("unused").roles("ATHLETE").build();

        assertThat(replacement.extractUsername(replacement.generateToken(user))).isEqualTo(user.getUsername());
        assertThat(replacement.extractUsername(old.generateToken(user))).isNull();
    }

    @Test
    void rejectsExpiredTokensAndTokensMissingRequiredClaims() {
        var key = Jwts.SIG.HS256.key().build();
        var service = new JwtService(Base64.getEncoder().encodeToString(key.getEncoded()), 60_000);
        Instant now = Instant.now();
        var expired = Jwts.builder().subject("fixture").expiration(Date.from(now.minusSeconds(60)));
        var noSubject = Jwts.builder().expiration(Date.from(now.plusSeconds(60)));
        var noExpiry = Jwts.builder().subject("fixture");
        for (var claims : java.util.List.of(expired, noSubject, noExpiry)) {
            assertThat(service.extractUsername(claims.signWith(key).compact())).isNull();
        }
    }

    private static String randomKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
