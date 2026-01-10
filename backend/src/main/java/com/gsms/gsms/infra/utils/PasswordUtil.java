package com.gsms.gsms.infra.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 使用BCrypt算法进行密码哈希
 *
 * BCrypt优势：
 * 1. 自动加盐，每次生成的哈希值都不同
 * 2. 可调整计算强度（cost factor），当前设置为12
 * 3. 抗彩虹表攻击
 * 4. 行业标准，经过广泛验证
 */
public class PasswordUtil {

    /**
     * BCrypt密码编码器，强度设置为12（推荐值）
     * 强度每增加1，计算时间翻倍
     * - 10: 快速，适合测试
     * - 12: 推荐，平衡安全性和性能（约500ms）
     * - 14: 高安全，性能要求较高（约2秒）
     */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码（BCrypt哈希，格式：$2a$12$...）
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码（用户输入的明文密码）
     * @param encryptedPassword 加密后的密码（数据库中存储的BCrypt哈希）
     * @return 是否匹配
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encryptedPassword);
    }

    /**
     * 生成默认密码的BCrypt哈希（用于密码重置）
     * 默认密码：Admin123
     * @return Admin123的BCrypt哈希值
     *
     * 注意：由于BCrypt每次生成结果不同，这个方法仅供参考
     * 实际使用时应该直接调用 encrypt("Admin123")
     */
    public static String generateDefaultPasswordHash() {
        return encrypt("Admin123");
    }
}
