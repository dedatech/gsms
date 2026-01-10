package com.gsms.gsms.utils;

import com.gsms.gsms.infra.config.JwtProperties;
import com.gsms.gsms.infra.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 *
 * 注意：这个测试使用手动构造的JwtUtil实例（不依赖Spring容器）
 * 如果需要在Spring上下文中测试，请使用JwtPropertiesTest
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        properties = new JwtProperties();
        properties.setSecret("test_secret_key_for_unit_testing");
        properties.setExpiration(3600000L);  // 1小时

        // 手动创建JwtUtil实例（不依赖Spring容器）
        jwtUtil = new JwtUtil(properties);
        jwtUtil.init();  // 设置静态引用
    }

    @Test
    void testGenerateAndValidateToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";

        // When
        String token = jwtUtil.generateToken(userId, username);

        // Then
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testGetUserIdFromToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtUtil.generateToken(userId, username);

        // When
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtUtil.generateToken(userId, username);

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetUserIdFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        Long userId = jwtUtil.getUserIdFromToken(invalidToken);

        // Then
        assertNull(userId);
    }

    @Test
    void testGetUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        String username = jwtUtil.getUsernameFromToken(invalidToken);

        // Then
        assertNull(username);
    }

    @Test
    void testStaticCompatibilityLayer() {
        // Given
        Long userId = 1L;
        String username = "testuser";

        // When - 使用静态方法
        String token = JwtUtil.generateTokenStatic(userId, username);

        // Then
        assertNotNull(token);
        assertTrue(JwtUtil.validateTokenStatic(token));
        assertEquals(userId, JwtUtil.getUserIdFromTokenStatic(token));
        assertEquals(username, JwtUtil.getUsernameFromTokenStatic(token));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // Given - 创建一个即将过期的token
        JwtProperties shortLivedProperties = new JwtProperties();
        shortLivedProperties.setSecret("test_secret");
        shortLivedProperties.setExpiration(1L);  // 1毫秒

        JwtUtil shortLivedUtil = new JwtUtil(shortLivedProperties);
        shortLivedUtil.init();

        String token = shortLivedUtil.generateToken(1L, "testuser");

        // When - 等待过期
        Thread.sleep(10);

        // Then
        assertFalse(shortLivedUtil.validateToken(token));
    }
}