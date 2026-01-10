package com.gsms.gsms.config;

import com.gsms.gsms.infra.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT配置属性测试
 * 验证配置正确加载
 */
@SpringBootTest
@ActiveProfiles("test")
class JwtPropertiesTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void testJwtPropertiesLoaded() {
        assertNotNull(jwtProperties);
        assertNotNull(jwtProperties.getSecret());
        assertTrue(jwtProperties.getExpiration() > 0);

        // 测试环境特定值
        assertEquals("gsms_test_secret_key_for_testing_only", jwtProperties.getSecret());
        assertEquals(3600000L, jwtProperties.getExpiration());
    }

    @Test
    void testJwtSecretNotBlank() {
        String secret = jwtProperties.getSecret();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
        assertFalse(secret.trim().isEmpty());
    }

    @Test
    void testJwtExpirationPositive() {
        Long expiration = jwtProperties.getExpiration();
        assertNotNull(expiration);
        assertTrue(expiration > 0);
    }
}
