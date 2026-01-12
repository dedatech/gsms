package com.gsms.gsms.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "更新角色请求")
public class RoleUpdateReq {

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long id;

    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称")
    private String name;

    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Schema(description = "角色编码")
    private String code;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "角色级别 1:系统级 2:项目级")
    private Integer roleLevel;

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

    public Integer getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(Integer roleLevel) {
        this.roleLevel = roleLevel;
    }
}
