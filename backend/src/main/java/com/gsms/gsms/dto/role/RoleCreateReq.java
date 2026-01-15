package com.gsms.gsms.dto.role;

import com.gsms.gsms.model.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "创建角色请求")
public class RoleCreateReq {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Schema(description = "角色编码", example = "PROJECT_MANAGER")
    private String code;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    @Schema(description = "角色描述")
    private String description;

    @NotNull(message = "角色类型不能为空")
    @Schema(description = "角色类型 SYSTEM:系统预置(不可删除) CUSTOM:用户自定义(可删除)", example = "CUSTOM")
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
}
