package com.gsms.gsms.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息响应
 */
@Schema(description = "用户信息响应")
public class UserInfoResp {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "电话")
    private String phone;
    
    @Schema(description = "用户状态")
    private UserStatus status;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    // Getter and Setter
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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    /**
     * 将 User 实体转换为 UserInfoResp
     */
    public static UserInfoResp from(User user) {
        if (user == null) {
            return null;
        }
        
        UserInfoResp resp = new UserInfoResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        
        return resp;
    }
    
    /**
     * 将 User 列表转换为 UserInfoResp 列表
     */
    public static java.util.List<UserInfoResp> from(List<User> users) {
        if (users == null) {
            return java.util.Collections.emptyList();
        }
        
        return users.stream()
                .map(UserInfoResp::from)
                .collect(Collectors.toList());
    }
}
