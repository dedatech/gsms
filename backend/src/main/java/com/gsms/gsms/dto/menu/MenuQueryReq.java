package com.gsms.gsms.dto.menu;

import com.gsms.gsms.dto.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 菜单查询请求
 */
@Schema(description = "菜单查询请求")
public class MenuQueryReq extends BasePageQuery {

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单类型 1:目录 2:菜单 3:按钮")
    private Integer type;

    @Schema(description = "状态 1:启用 2:禁用")
    private Integer status;

    @Schema(description = "父菜单ID")
    private Long parentId;

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
