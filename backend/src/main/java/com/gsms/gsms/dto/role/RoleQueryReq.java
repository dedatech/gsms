package com.gsms.gsms.dto.role;

import com.gsms.gsms.dto.BasePageQuery;
import com.gsms.gsms.model.enums.RoleType;
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

    @Schema(description = "角色类型 SYSTEM:系统预置 CUSTOM:用户自定义", example = "CUSTOM")
    private RoleType roleType;

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

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }
}
