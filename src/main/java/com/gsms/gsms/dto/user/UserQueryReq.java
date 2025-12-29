package com.gsms.gsms.dto.user;

import com.gsms.gsms.dto.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户查询请求
 */
@Schema(description = "用户查询请求")
public class UserQueryReq extends BasePageQuery {

    @Schema(description = "用户名（模糊匹配）", example = "admin")
    private String username;

    @Schema(description = "用户状态", example = "1")
    private Integer status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}