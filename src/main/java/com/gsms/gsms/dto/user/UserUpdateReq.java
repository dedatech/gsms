package com.gsms.gsms.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户更新请求
 */
@Schema(description = "用户更新请求")
public class UserUpdateReq {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @Schema(description = "密码", example = "123456")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "电话", example = "13800138000")
    private String phone;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
