package com.gsms.gsms.dto.permission;

import com.gsms.gsms.dto.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 权限查询请求
 */
@Schema(description = "权限查询请求")
public class PermissionQueryReq extends BasePageQuery {

    @Schema(description = "权限名称（模糊匹配）", example = "查看项目")
    private String name;

    @Schema(description = "权限编码（模糊匹配）", example = "PROJECT_VIEW")
    private String code;

    @Schema(description = "权限类型 1:功能权限 2:菜单权限 3:数据权限", example = "1")
    private Integer permissionType;

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

    public Integer getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
    }
}
