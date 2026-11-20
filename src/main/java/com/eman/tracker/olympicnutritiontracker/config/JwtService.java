package com.eman.tracker.olympicnutritiontracker.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final JwtParser parser;
    private final long jwtExpiration;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long jwtExpiration) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT_SECRET must be Base64-encoded");
        }
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must decode to at least 32 bytes");
        }
        if (jwtExpiration <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(key);
        this.parser = Jwts.parser().verifyWith(signingKey).build();
        this.jwtExpiration = jwtExpiration;
    }

    public String extractUsername(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            String subject = claims.getSubject();
            return claims.getExpiration() == null || subject == null || subject.isBlank() ? null : subject;
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtExpiration)))
                .signWith(signingKey)
                .compact();
    }
}
