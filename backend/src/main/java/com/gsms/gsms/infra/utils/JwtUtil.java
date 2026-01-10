package com.gsms.gsms.infra.utils;

import com.gsms.gsms.infra.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于身份认证
 *
 * 重构说明：
 * 1. 从静态工具类改为Spring Bean，支持依赖注入
 * 2. 配置通过JwtProperties注入，支持多环境
 * 3. 保留静态方法作为兼容层，委托给实例方法
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // 静态引用，用于向后兼容
    private static JwtUtil instance;

    // Spring注入的配置
    private final JwtProperties properties;

    // 构造函数注入（推荐）
    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        logger.info("JwtUtil初始化 - 过期时间: {}ms", properties.getExpiration());
    }

    /**
     * 初始化后设置静态引用
     * 保证静态方法可以访问到配置
     */
    @PostConstruct
    public void init() {
        instance = this;
        logger.info("JwtUtil静态引用已设置");
    }

    // ========== 实例方法（推荐使用） ==========

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @return Token字符串
     */
    public String generateToken(Long userId, String username) {
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
    public String generateToken(Map<String, Object> claims) {
        Date expireDate = new Date(System.currentTimeMillis() + properties.getExpiration());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, properties.getSecret())
                .compact();
    }

    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(properties.getSecret()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.debug("Token验证失败: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(properties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            logger.debug("从Token获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     * @param token Token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(properties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("username").toString();
        } catch (Exception e) {
            logger.debug("从Token获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    // ========== 静态方法（兼容层，已废弃） ==========

    /**
     * 生成JWT Token（静态方法，已废弃）
     * @deprecated 使用实例方法 {@link #generateToken(Long, String)}
     */
    @Deprecated
    public static String generateTokenStatic(Long userId, String username) {
        if (instance == null) {
            throw new IllegalStateException("JwtUtil未初始化，请确保Spring容器已启动");
        }
        return instance.generateToken(userId, username);
    }

    /**
     * 验证Token（静态方法，已废弃）
     * @deprecated 使用实例方法 {@link #validateToken(String)}
     */
    @Deprecated
    public static boolean validateTokenStatic(String token) {
        if (instance == null) {
            throw new IllegalStateException("JwtUtil未初始化，请确保Spring容器已启动");
        }
        return instance.validateToken(token);
    }

    /**
     * 从Token获取用户ID（静态方法，已废弃）
     * @deprecated 使用实例方法 {@link #getUserIdFromToken(String)}
     */
    @Deprecated
    public static Long getUserIdFromTokenStatic(String token) {
        if (instance == null) {
            throw new IllegalStateException("JwtUtil未初始化，请确保Spring容器已启动");
        }
        return instance.getUserIdFromToken(token);
    }

    /**
     * 从Token获取用户名（静态方法，已废弃）
     * @deprecated 使用实例方法 {@link #getUsernameFromToken(String)}
     */
    @Deprecated
    public static String getUsernameFromTokenStatic(String token) {
        if (instance == null) {
            throw new IllegalStateException("JwtUtil未初始化，请确保Spring容器已启动");
        }
        return instance.getUsernameFromToken(token);
    }
}