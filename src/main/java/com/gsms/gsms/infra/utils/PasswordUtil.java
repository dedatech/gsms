package com.gsms.gsms.infra.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码工具类
 */
public class PasswordUtil {
    
    /**
     * MD5加密
     */
    public static String encrypt(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return encrypt(rawPassword).equals(encryptedPassword);
    }
}
