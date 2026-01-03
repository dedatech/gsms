package com.gsms.gsms.dto.user;

import com.gsms.gsms.dto.BasePageQuery;
import com.gsms.gsms.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户查询请求
 */
@Schema(description = "用户查询请求")
public class UserQueryReq extends BasePageQuery {

    @Schema(description = "用户名（模糊匹配）", example = "admin")
    private String username;

    @Schema(description = "用户状态", example = "NORMAL")
    private UserStatus status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}