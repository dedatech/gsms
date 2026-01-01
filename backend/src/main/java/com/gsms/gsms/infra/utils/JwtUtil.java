package com.gsms.gsms.infra.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于身份认证
 */
public class JwtUtil {
    // 设置默认过期时间为24小时
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;
    // 设置密钥明文
    private static final String SECRET = "gsms_secret_key_2025";

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @return Token字符串
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims);
    }

    /**
     * 生成JWT Token
     * @param claims 自定义载荷
     * @return Token字符串
     */
    public static String generateToken(Map<String, Object> claims) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            // 使用JWT解析器设置签名密钥，并尝试解析传入的token
            // 如果token无效（如过期、签名不匹配等），将抛出异常
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 记录验证失败的具体原因，方便排查问题
            System.err.println("Token验证失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     * @param token Token字符串
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
            return claims.get("username").toString();
        } catch (Exception e) {
            return null;
        }
    }
}