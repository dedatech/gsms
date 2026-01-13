package com.gsms.gsms.dto.project;

import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.model.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 项目创建请求（继承公共字段）
 */
@Schema(description = "项目创建请求")
public class ProjectCreateReq {
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    @Schema(description = "项目名称", example = "工时管理系统")
    private String name;

    @NotBlank(message = "项目编码不能为空")
    @Size(max = 50, message = "项目编码长度不能超过50个字符")
    @Schema(description = "项目编码", example = "GSMS")
    private String code;

    @NotNull(message = "项目类型不能为空")
    @Schema(description = "项目类型", example = "SCHEDULE")
    private ProjectType projectType;

    @Schema(description = "项目描述", example = "企业级工时管理平台")
    private String description;

    @NotNull(message = "项目负责人不能为空")
    @Schema(description = "项目负责人ID", example = "1")
    private Long managerId;

    @Schema(description = "项目状态", example = "IN_PROGRESS")
    private ProjectStatus status;

    /**
     * 计划开始日期
     */

    @Schema(description = "计划开始日期（格式：yyyy-MM-dd）", example = "2025-01-01", required = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    @Schema(description = "计划结束日期（格式：yyyy-MM-dd）", example = "2025-12-31")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

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

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
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

    public LocalDate getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(LocalDate planStartDate) {
        this.planStartDate = planStartDate;
    }

    public LocalDate getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(LocalDate planEndDate) {
        this.planEndDate = planEndDate;
    }
}
