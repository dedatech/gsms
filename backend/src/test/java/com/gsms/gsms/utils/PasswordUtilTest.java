package com.gsms.gsms.utils;

import com.gsms.gsms.infra.utils.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCrypt密码工具类测试
 */
class PasswordUtilTest {

    @Test
    @DisplayName("BCrypt加密密码应该生成不同的哈希值")
    void testEncrypt_GeneratesDifferentHashes() {
        String password = "Admin123";

        // BCrypt每次生成不同的哈希值（因为随机盐）
        String hash1 = PasswordUtil.encrypt(password);
        String hash2 = PasswordUtil.encrypt(password);

        System.out.println("Hash1: " + hash1);
        System.out.println("Hash2: " + hash2);

        // 两次加密结果应该不同
        assertNotEquals(hash1, hash2);

        // 但都应该以$2a$12$开头（BCrypt格式）
        assertTrue(hash1.startsWith("$2a$12$"));
        assertTrue(hash2.startsWith("$2a$12$"));
    }

    @Test
    @DisplayName("BCrypt验证密码应该正确匹配")
    void testVerify_ShouldMatch() {
        String password = "Admin123";
        String hash = PasswordUtil.encrypt(password);

        // 应该能正确验证密码
        assertTrue(PasswordUtil.verify(password, hash));

        // 错误的密码应该不匹配
        assertFalse(PasswordUtil.verify("wrongpassword", hash));
    }

    @Test
    @DisplayName("BCrypt验证应该匹配正确的密码")
    void testVerify_MatchCorrectPassword() {
        String password = "Admin123";
        String hash = PasswordUtil.encrypt(password);

        // 应该能验证正确的密码
        assertTrue(PasswordUtil.verify(password, hash), "正确的密码应该匹配");

        // 错误的密码应该不匹配
        assertFalse(PasswordUtil.verify("Admin124", hash), "错误的密码不应该匹配");
    }

    @Test
    @DisplayName("空值处理应该正确")
    void testNullHandling() {
        // null或空字符串应该返回null
        assertNull(PasswordUtil.encrypt(null));
        assertNull(PasswordUtil.encrypt(""));

        // 验证null值应该返回false
        assertFalse(PasswordUtil.verify("test", null));
        assertFalse(PasswordUtil.verify(null, "hash"));
        assertFalse(PasswordUtil.verify(null, null));
    }

    @Test
    @DisplayName("BCrypt性能测试")
    void testPerformance() {
        String password = "Admin123";
        int iterations = 10;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            PasswordUtil.encrypt(password);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        long avgTime = duration / iterations;

        System.out.println("BCrypt加密" + iterations + "次总耗时: " + duration + "ms");
        System.out.println("平均每次加密耗时: " + avgTime + "ms");

        // BCrypt(12)每次加密应该在100-1000ms之间
        assertTrue(avgTime >= 100 && avgTime <= 2000,
            "BCrypt加密时间应该在100-2000ms之间，实际: " + avgTime + "ms");
    }
}
