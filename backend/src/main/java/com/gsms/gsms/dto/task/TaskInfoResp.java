package com.gsms.gsms.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.enums.TaskPriority;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.model.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务信息响应
 */
@Schema(description = "任务信息响应")
public class TaskInfoResp {
    
    @Schema(description = "任务ID")
    private Long id;
    
    @Schema(description = "项目ID")
    private Long projectId;
    
    @Schema(description = "迭代ID")
    private Long iterationId;
    
    @Schema(description = "任务标题")
    private String title;
    
    @Schema(description = "任务描述")
    private String description;
    
    @Schema(description = "任务类型")
    private TaskType type;
    
    @Schema(description = "优先级")
    private TaskPriority priority;
    
    @Schema(description = "负责人ID")
    private Long assigneeId;
    
    @Schema(description = "任务状态")
    private TaskStatus status;
    
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
    
    // Getter and Setter
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
    
    /**
     * 将 Task 实体转换为 TaskInfoResp
     */
    public static TaskInfoResp from(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskInfoResp resp = new TaskInfoResp();
        resp.setId(task.getId());
        resp.setProjectId(task.getProjectId());
        resp.setIterationId(task.getIterationId());
        resp.setTitle(task.getTitle());
        resp.setDescription(task.getDescription());
        resp.setType(task.getType());
        resp.setPriority(task.getPriority());
        resp.setAssigneeId(task.getAssigneeId());
        resp.setStatus(task.getStatus());
        resp.setPlanStartDate(task.getPlanStartDate());
        resp.setPlanEndDate(task.getPlanEndDate());
        resp.setActualStartDate(task.getActualStartDate());
        resp.setActualEndDate(task.getActualEndDate());
        
        return resp;
    }
    
    /**
     * 将 Task 列表转换为 TaskInfoResp 列表
     */
    public static java.util.List<TaskInfoResp> from(List<Task> tasks) {
        if (tasks == null) {
            return java.util.Collections.emptyList();
        }
        
        return tasks.stream()
                .map(TaskInfoResp::from)
                .collect(Collectors.toList());
    }
}