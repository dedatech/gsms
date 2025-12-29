package com.gsms.gsms.dto.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目信息响应
 */
@Schema(description = "项目信息响应")
public class ProjectInfoResp {

    @Schema(description = "项目ID")
    private Long id;

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "项目编码")
    private String code;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "项目负责人ID")
    private Long managerId;

    @Schema(description = "项目状态")
    private ProjectStatus status;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date planStartDate;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date planEndDate;

    @Schema(description = "实际开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date actualStartDate;

    @Schema(description = "实际结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date actualEndDate;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    // Getter and Setter
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

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 将 Project 实体转换为 ProjectInfoResp
     */
    public static ProjectInfoResp from(Project project) {
        if (project == null) {
            return null;
        }

        ProjectInfoResp resp = new ProjectInfoResp();
        resp.setId(project.getId());
        resp.setName(project.getName());
        resp.setCode(project.getCode());
        resp.setDescription(project.getDescription());
        resp.setManagerId(project.getManagerId());
        resp.setStatus(project.getStatus());
        resp.setPlanStartDate(project.getPlanStartDate());
        resp.setPlanEndDate(project.getPlanEndDate());
        resp.setActualStartDate(project.getActualStartDate());
        resp.setActualEndDate(project.getActualEndDate());
        resp.setCreateUserId(project.getCreateUserId());
        resp.setCreateTime(project.getCreateTime());
        resp.setUpdateTime(project.getUpdateTime());

        return resp;
    }

    /**
     * 将 Project 列表转换为 ProjectInfoResp 列表
     */
    public static List<ProjectInfoResp> from(List<Project> projects) {
        if (projects == null) {
            return java.util.Collections.emptyList();
        }

        return projects.stream()
                .map(ProjectInfoResp::from)
                .collect(Collectors.toList());
    }
}
