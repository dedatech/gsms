package com.gsms.gsms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 任务实体类
 */
@Schema(description = "任务信息")
public class Task {
    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private Long id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 迭代ID
     */
    @Schema(description = "迭代ID")
    private Long iterationId;

    /**
     * 任务标题
     */
    @Schema(description = "任务标题")
    private String title;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述")
    private String description;

    /**
     * 任务类型 1:任务 2:需求 3:缺陷
     */
    @Schema(description = "任务类型 1:任务 2:需求 3:缺陷")
    private Integer type;

    /**
     * 优先级 1:高 2:中 3:低
     */
    @Schema(description = "优先级 1:高 2:中 3:低")
    private Integer priority;

    /**
     * 负责人ID
     */
    @Schema(description = "负责人ID")
    private Long assigneeId;

    /**
     * 任务状态 1:待处理 2:进行中 3:已完成
     */
    @Schema(description = "任务状态 1:待处理 2:进行中 3:已完成")
    private Integer status;

    /**
     * 计划开始日期
     */
    @Schema(description = "计划开始日期")
    private Date planStartDate;

    /**
     * 计划结束日期
     */
    @Schema(description = "计划结束日期")
    private Date planEndDate;

    /**
     * 实际开始日期
     */
    @Schema(description = "实际开始日期")
    private Date actualStartDate;

    /**
     * 实际结束日期
     */
    @Schema(description = "实际结束日期")
    private Date actualEndDate;

    /**
     * 预估工时
     */
    @Schema(description = "预估工时")
    private BigDecimal estimateHours;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createUserId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getIterationId() {
        return iterationId;
    }

    public void setIterationId(Long iterationId) {
        this.iterationId = iterationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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

    public BigDecimal getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(BigDecimal estimateHours) {
        this.estimateHours = estimateHours;
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
}