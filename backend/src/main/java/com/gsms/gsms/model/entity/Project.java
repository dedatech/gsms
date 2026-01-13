package com.gsms.gsms.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.model.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体类
 */
@Schema(description = "项目信息")
public class Project {
    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String name;

    /**
     * 项目编码
     */
    @Schema(description = "项目编码")
    private String code;

    /**
     * 项目类型
     */
    @Schema(description = "项目类型")
    @TableField("project_type")
    private ProjectType projectType = ProjectType.SCHEDULE; // 默认常规型

    /**
     * 项目描述
     */
    @Schema(description = "项目描述")
    private String description;

    /**
     * 项目负责人ID
     */
    @Schema(description = "项目负责人ID")
    private Long managerId;

    /**
     * 项目状态
     */
    @Schema(description = "项目状态")
    private ProjectStatus status;

    /**
     * 计划开始日期
     */
    @Schema(description = "计划开始日期", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    /**
     * 计划结束日期
     */
    @Schema(description = "计划结束日期", example = "2024-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

    /**
     * 实际开始日期
     */
    @Schema(description = "实际开始日期", example = "2024-01-05")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    /**
     * 实际结束日期
     */
    @Schema(description = "实际结束日期", example = "2024-12-25")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createUserId;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Long updateUserId;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    private Integer isDeleted;
    
    // Getter和Setter方法
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

    public LocalDate getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(LocalDate actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Long getUpdateUserId() {
        return updateUserId;
    }
    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
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
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}