package com.student.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(
            "student-management-test-secret-key-2026-09-05",
            7_200_000L);

    @Test
    void createsAndParsesTokenWithUniqueIdentifier() {
        String token = jwtService.createToken(7L, "tester", "ADMIN");

        Claims claims = jwtService.parse(token);

        assertEquals("7", claims.getSubject());
        assertEquals("tester", claims.get("username", String.class));
        assertEquals("ADMIN", claims.get("role", String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        String token = new JwtService(
                "another-test-secret-key-2026-09-05",
                7_200_000L).createToken(7L, "tester", "ADMIN");

        assertThrows(Exception.class, () -> jwtService.parse(token));
    }
}
