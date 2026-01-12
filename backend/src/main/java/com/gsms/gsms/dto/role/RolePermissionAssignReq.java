package com.gsms.gsms.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色权限分配请求
 */
@Schema(description = "角色权限分配请求")
public class RolePermissionAssignReq {

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long roleId;

    @NotEmpty(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
