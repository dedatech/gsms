package com.gsms.gsms.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 重置密码请求DTO（管理员）
 */
@Schema(description = "重置密码请求")
public class PasswordResetReq {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", required = true, example = "1")
    private Long userId;

    /**
     * 新密码
     */
    @Schema(description = "新密码（6-20位）", required = true, example = "newPassword123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    private String newPassword;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
