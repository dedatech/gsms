package com.gsms.gsms.dto.department;

import com.gsms.gsms.dto.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 部门查询请求
 */
@Schema(description = "部门查询请求")
public class DepartmentQueryReq extends BasePageQuery {

    @Schema(description = "部门名称（模糊匹配）", example = "技术部")
    private String name;

    @Schema(description = "父部门ID", example = "1")
    private Long parentId;

    // Getter and Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}