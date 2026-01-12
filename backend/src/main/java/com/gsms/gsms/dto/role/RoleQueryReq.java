package com.gsms.gsms.dto.role;

import com.gsms.gsms.dto.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 角色查询请求
 */
@Schema(description = "角色查询请求")
public class RoleQueryReq extends BasePageQuery {

    @Schema(description = "角色名称（模糊匹配）", example = "管理员")
    private String name;

    @Schema(description = "角色编码（模糊匹配）", example = "ADMIN")
    private String code;

    @Schema(description = "角色级别 1:系统级 2:项目级", example = "1")
    private Integer roleLevel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(Integer roleLevel) {
        this.roleLevel = roleLevel;
    }
}
