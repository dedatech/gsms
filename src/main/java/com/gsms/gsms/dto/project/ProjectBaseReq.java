package com.gsms.gsms.dto.project;

import com.gsms.gsms.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 项目请求基类 - 抽取公共字段
 */
public abstract class ProjectBaseReq {
    
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    @Schema(description = "项目名称", required = true, example = "工时管理系统")
    private String name;
    
    @NotBlank(message = "项目编码不能为空")
    @Size(max = 50, message = "项目编码长度不能超过50个字符")
    @Schema(description = "项目编码", required = true, example = "GSMS")
    private String code;
    
    @Schema(description = "项目描述", example = "企业级工时管理平台")
    private String description;
    
    @NotNull(message = "项目负责人不能为空")
    @Schema(description = "项目负责人ID", required = true, example = "1")
    private Long managerId;
    
    @Schema(description = "项目状态", example = "IN_PROGRESS")
    private ProjectStatus status;
    
    @Schema(description = "计划开始日期", example = "2025-01-01")
    private Date planStartDate;
    
    @Schema(description = "计划结束日期", example = "2025-12-31")
    private Date planEndDate;

    // Getter and Setter
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

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Date getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(Date planStartDate) {
        this.planStartDate = planStartDate;
    }

    public Date getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(Date planEndDate) {
        this.planEndDate = planEndDate;
    }
}
