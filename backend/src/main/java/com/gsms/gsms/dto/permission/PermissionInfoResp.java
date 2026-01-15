package com.gsms.gsms.dto.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限信息响应
 */
@Schema(description = "权限信息响应")
public class PermissionInfoResp {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "权限编码")
    private String code;

    @Schema(description = "权限描述")
    private String description;

    @Schema(description = "权限类型 1:功能权限 2:菜单权限 3:数据权限")
    private Integer permissionType;

    @Schema(description = "创建时间", type = "string", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", type = "string", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "拥有该权限的角色ID列表")
    private List<Long> roleIds;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    /**
     * 将 Permission 实体转换为 PermissionInfoResp
     */
    public static PermissionInfoResp from(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionInfoResp resp = new PermissionInfoResp();
        resp.setId(permission.getId());
        resp.setName(permission.getName());
        resp.setCode(permission.getCode());
        resp.setDescription(permission.getDescription());
        resp.setPermissionType(permission.getPermissionType());
        resp.setCreateTime(permission.getCreateTime());
        resp.setUpdateTime(permission.getUpdateTime());

        return resp;
    }

    /**
     * 将 Permission 列表转换为 PermissionInfoResp 列表
     */
    public static List<PermissionInfoResp> from(List<Permission> permissions) {
        if (permissions == null) {
            return java.util.Collections.emptyList();
        }

        return permissions.stream()
                .map(PermissionInfoResp::from)
                .collect(Collectors.toList());
    }
}
