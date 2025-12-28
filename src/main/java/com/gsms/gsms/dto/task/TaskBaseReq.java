package com.gsms.gsms.dto.task;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.domain.enums.TaskPriority;
import com.gsms.gsms.domain.enums.TaskStatus;
import com.gsms.gsms.domain.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 任务请求基类 - 抽取公共字段
 */
public abstract class TaskBaseReq {
    
    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID", example = "1")
    private Long projectId;
    
    @Schema(description = "迭代ID", example = "1")
    private Long iterationId;
    
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
    
    @Schema(description = "计划开始日期", example = "2025-01-01")
    private Date planStartDate;
    
    @Schema(description = "计划结束日期", example = "2025-01-10")
    private Date planEndDate;
    
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

    public BigDecimal getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(BigDecimal estimateHours) {
        this.estimateHours = estimateHours;
    }

    /**
     * 任务对象转换器
     */
    public static class TaskConverter {

        /**
         * 创建请求转任务实体
         */
        public static Task toTask(TaskCreateReq req) {
            if (req == null) {
                return null;
            }
            Task task = new Task();
            task.setProjectId(req.getProjectId());
            task.setIterationId(req.getIterationId());
            task.setTitle(req.getTitle());
            task.setDescription(req.getDescription());
            task.setType(req.getType());  // 直接设置枚举，MyBatis-Plus自动转换
            task.setPriority(req.getPriority());
            task.setAssigneeId(req.getAssigneeId());
            task.setStatus(req.getStatus());
            task.setPlanStartDate(req.getPlanStartDate());
            task.setPlanEndDate(req.getPlanEndDate());
            task.setEstimateHours(req.getEstimateHours());
            return task;
        }

        /**
         * 更新请求转任务实体
         */
        public static Task toTask(TaskUpdateReq req) {
            if (req == null) {
                return null;
            }
            Task task = new Task();
            task.setId(req.getId());
            task.setProjectId(req.getProjectId());
            task.setIterationId(req.getIterationId());
            task.setTitle(req.getTitle());
            task.setDescription(req.getDescription());
            task.setType(req.getType());  // 直接设置枚举，MyBatis-Plus自动转换
            task.setPriority(req.getPriority());
            task.setAssigneeId(req.getAssigneeId());
            task.setStatus(req.getStatus());
            task.setPlanStartDate(req.getPlanStartDate());
            task.setPlanEndDate(req.getPlanEndDate());
            task.setActualStartDate(req.getActualStartDate());
            task.setActualEndDate(req.getActualEndDate());
            task.setEstimateHours(req.getEstimateHours());
            return task;
        }
    }
}
