package com.gsms.gsms.dto.task;

import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.enums.TaskPriority;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.model.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 任务请求基类 - 抽取公共字段
 */
public abstract class TaskBaseReq {
    
    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    @Schema(description = "迭代ID", example = "1")
    private Long iterationId;

    @Schema(description = "父任务ID（用于创建子任务）", example = "1")
    private Long parentId;
    
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题长度不能超过200个字符")
    @Schema(description = "任务标题", example = "实现用户登录功能")
    private String title;
    
    @Schema(description = "任务描述", example = "完成用户登录功能的前后端开发")
    private String description;
    
    @Schema(description = "任务类型", example = "TASK")
    private TaskType type;
    
    @Schema(description = "优先级", example = "MEDIUM")
    private TaskPriority priority;
    
    @Schema(description = "负责人ID", example = "1")
    private Long assigneeId;
    
    @Schema(description = "任务状态", example = "TODO")
    private TaskStatus status;

    @Schema(description = "计划开始日期（格式：yyyy-MM-dd）", example = "2025-01-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    @Schema(description = "计划结束日期（格式：yyyy-MM-dd）", example = "2025-01-10")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;
    
    @Schema(description = "预估工时", example = "8.0")
    private BigDecimal estimateHours;

    // Getter and Setter
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public BigDecimal getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(BigDecimal estimateHours) {
        this.estimateHours = estimateHours;
    }
}
