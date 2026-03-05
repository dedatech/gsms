package com.gsms.gsms.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求
 */
@Schema(description = "用户登录请求")
public class UserLoginReq {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "admin123")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "A3B9")
    private String captchaCode;

    @NotBlank(message = "验证码UUID不能为空")
    @Schema(description = "验证码UUID", example = "uuid-xxx-xxx")
    private String captchaUuid;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getCaptchaUuid() {
        return captchaUuid;
    }

    public void setCaptchaUuid(String captchaUuid) {
        this.captchaUuid = captchaUuid;
    }
}
