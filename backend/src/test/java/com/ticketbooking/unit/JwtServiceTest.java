package com.ticketbooking.unit;

import com.ticketbooking.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-jwt-at-least-32-bytes-long";

    @Test
    void createdTokenContainsEmailAndRole() {
        JwtService jwtService = new JwtService(SECRET, 60);

        String token = jwtService.createToken("amina@gmail.com", "ADMIN");
        Claims claims = jwtService.readToken(token);

        assertThat(claims.getSubject()).isEqualTo("amina@gmail.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwtService = new JwtService(SECRET, -5);

        String token = jwtService.createToken("amina@gmail.com", "CUSTOMER");

        assertThrows(ExpiredJwtException.class, () -> jwtService.readToken(token));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService ourService = new JwtService(SECRET, 60);
        JwtService otherService = new JwtService("another-secret-key-for-jwt-at-least-32-bytes", 60);

        String foreignToken = otherService.createToken("hacker@mail.kz", "ADMIN");

        assertThrows(JwtException.class, () -> ourService.readToken(foreignToken));
    }
}
