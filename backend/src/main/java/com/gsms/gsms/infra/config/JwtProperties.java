package com.gsms.gsms.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * JWT配置属性类
 * 从application.yml中读取jwt.*配置
 *
 * 支持环境变量覆盖：
 * - JWT_SECRET: JWT密钥
 * - JWT_EXPIRATION: Token过期时间（毫秒）
 */
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

    /**
     * JWT签名密钥
     * 生产环境必须通过环境变量设置
     */
    @NotBlank(message = "JWT密钥不能为空")
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 默认24小时 = 86400000ms
     */
    @Positive(message = "过期时间必须为正数")
    private Long expiration = 24 * 60 * 60 * 1000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
