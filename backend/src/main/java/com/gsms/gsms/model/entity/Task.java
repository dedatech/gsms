package com.gsms.gsms.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.enums.TaskPriority;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.model.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
     * 项目名称（非数据库字段，用于关联查询）
     */
    @Schema(description = "项目名称")
    @TableField(exist = false)
    private String projectName;

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
     * 任务类型
     */
    @Schema(description = "任务类型")
    private TaskType type;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private TaskPriority priority;

    /**
     * 负责人ID
     */
    @Schema(description = "负责人ID")
    private Long assigneeId;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态")
    private TaskStatus status;

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
    @Schema(description = "计划结束日期", example = "2024-01-10")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

    /**
     * 实际开始日期
     */
    @Schema(description = "实际开始日期", example = "2024-01-02")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDate actualStartDate;

    /**
     * 实际结束日期
     */
    @Schema(description = "实际结束日期", example = "2024-01-09")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDate actualEndDate;

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
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Long updateUserId;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
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