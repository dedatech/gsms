package com.gsms.gsms.utils;

import com.gsms.gsms.infra.utils.JwtUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
class JwtUtilTest {

    @Test
    void testGenerateAndValidateToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";

        // When
        String token = JwtUtil.generateToken(userId, username);

        // Then
        assertNotNull(token);
        assertTrue(JwtUtil.validateToken(token));
    }

    @Test
    void testGetUserIdFromToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String token = JwtUtil.generateToken(userId, username);

        // When
        Long extractedUserId = JwtUtil.getUserIdFromToken(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String token = JwtUtil.generateToken(userId, username);

        // When
        String extractedUsername = JwtUtil.getUsernameFromToken(token);

        // Then
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean isValid = JwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetUserIdFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        Long userId = JwtUtil.getUserIdFromToken(invalidToken);

        // Then
        assertNull(userId);
    }

    @Test
    void testGetUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        String username = JwtUtil.getUsernameFromToken(invalidToken);

        // Then
        assertNull(username);
    }
}