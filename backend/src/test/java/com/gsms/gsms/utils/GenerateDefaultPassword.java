package com.gsms.gsms.utils;

import com.gsms.gsms.infra.utils.PasswordUtil;

/**
 * 生成默认密码的BCrypt哈希值
 * 用于数据库迁移脚本
 */
public class GenerateDefaultPassword {
    public static void main(String[] args) {
        String defaultPassword = "Admin123";

        System.out.println("=== 生成默认密码BCrypt哈希值 ===");
        System.out.println("默认密码: " + defaultPassword);
        System.out.println();

        // 生成3个示例哈希值（BCrypt每次生成不同结果）
        for (int i = 1; i <= 3; i++) {
            String hash = PasswordUtil.encrypt(defaultPassword);
            System.out.println("示例" + i + ": " + hash);

            // 验证生成的哈希值
            boolean verified = PasswordUtil.verify(defaultPassword, hash);
            System.out.println("验证结果: " + (verified ? "✓ 通过" : "✗ 失败"));
            System.out.println();
        }

        System.out.println("注意：由于BCrypt每次生成结果不同，以上任一哈希值都可用于迁移脚本");
        System.out.println("推荐使用第一个示例值");
    }
}
