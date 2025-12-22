package com.gsms.gsms.dto.project;

import com.gsms.gsms.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 项目查询请求
 */
@Schema(description = "项目查询请求")
public class ProjectQueryReq {
    
    @Schema(description = "项目名称，模糊匹配", example = "工时管理")
    private String name;
    
    @Schema(description = "项目状态", example = "IN_PROGRESS")
    private ProjectStatus status;

    // Getter and Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
