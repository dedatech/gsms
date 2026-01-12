package com.gsms.gsms.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "更新权限请求")
public class PermissionUpdateReq {

    @NotNull(message = "权限ID不能为空")
    @Schema(description = "权限ID")
    private Long id;

    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Schema(description = "权限名称")
    private String name;

    @Pattern(regexp = "^[A-Z_]+$", message = "权限编码只能包含大写字母和下划线")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    @Schema(description = "权限编码")
    private String code;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    @Schema(description = "权限描述")
    private String description;

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
}
