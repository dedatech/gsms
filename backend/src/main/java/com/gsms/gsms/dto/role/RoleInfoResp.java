package com.gsms.gsms.dto.role;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.Role;
import com.gsms.gsms.model.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色信息响应
 */
@Schema(description = "角色信息响应")
public class RoleInfoResp {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "角色类型 SYSTEM:系统预置(不可删除) CUSTOM:用户自定义(可删除)")
    private RoleType roleType;

    @Schema(description = "创建时间", type = "string", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", type = "string", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

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

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
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

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    /**
     * 将 Role 实体转换为 RoleInfoResp
     */
    public static RoleInfoResp from(Role role) {
        if (role == null) {
            return null;
        }

        RoleInfoResp resp = new RoleInfoResp();
        resp.setId(role.getId());
        resp.setName(role.getName());
        resp.setCode(role.getCode());
        resp.setDescription(role.getDescription());
        resp.setRoleType(role.getRoleType());
        resp.setCreateTime(role.getCreateTime());
        resp.setUpdateTime(role.getUpdateTime());

        return resp;
    }

    /**
     * 将 Role 列表转换为 RoleInfoResp 列表
     */
    public static List<RoleInfoResp> from(List<Role> roles) {
        if (roles == null) {
            return java.util.Collections.emptyList();
        }

        return roles.stream()
                .map(RoleInfoResp::from)
                .collect(Collectors.toList());
    }
}
