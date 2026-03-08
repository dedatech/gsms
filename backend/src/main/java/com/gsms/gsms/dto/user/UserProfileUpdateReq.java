package com.gsms.gsms.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户个人信息更新请求
 */
@Schema(description = "用户个人信息更新请求")
public class UserProfileUpdateReq {

    @Schema(description = "昵称", example = "张三")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "电话", example = "13800138000")
    private String phone;

    // Getters and Setters
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
