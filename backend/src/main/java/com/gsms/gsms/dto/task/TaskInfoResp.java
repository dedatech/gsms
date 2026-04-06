package com.gsms.gsms.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.enums.DefectSeverity;
import com.gsms.gsms.model.enums.TaskPriority;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.model.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Schema(description = "项目名称")
    private String projectName;
    
    @Schema(description = "迭代ID")
    private Long iterationId;

    @Schema(description = "迭代名称")
    private String iterationName;

    @Schema(description = "父任务ID")
    private Long parentId;

    @Schema(description = "预估工时")
    private BigDecimal estimateHours;

    @Schema(description = "实际工时（从工时记录汇总）")
    private BigDecimal actualHours;

    @Schema(description = "剩余工时（预估工时 - 实际工时）")
    private BigDecimal remainingHours;

    @Schema(description = "子任务列表（嵌套结构）")
    private java.util.List<TaskInfoResp> subtasks;

    @Schema(description = "任务标题")
    private String title;
    
    @Schema(description = "任务描述")
    private String description;
    
    @Schema(description = "任务类型")
    private TaskType type;
    
    @Schema(description = "优先级")
    private TaskPriority priority;

    // 缺陷特有字段
    @Schema(description = "缺陷严重程度（仅缺陷类型使用）")
    private DefectSeverity severity;

    @Schema(description = "缺陷复现步骤（仅缺陷类型使用）")
    private String reproductionSteps;

    @Schema(description = "附件列表（JSON格式）")
    private String attachments;

    @Schema(description = "修复版本")
    private String fixVersion;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人姓名")
    private String assigneeName;

    @Schema(description = "任务状态")
    private TaskStatus status;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

    @Schema(description = "实际开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @Schema(description = "实际结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建人姓名")
    private String createUserName;

    @Schema(description = "更新人ID")
    private Long updateUserId;

    @Schema(description = "更新人姓名")
    private String updateUserName;

    @Schema(description = "创建时间", type = "string", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", type = "string", example = "2024-01-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

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

    public String getIterationName() {
        return iterationName;
    }

    public void setIterationName(String iterationName) {
        this.iterationName = iterationName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public BigDecimal getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(BigDecimal estimateHours) {
        this.estimateHours = estimateHours;
    }

    public BigDecimal getActualHours() {
        return actualHours;
    }

    public void setActualHours(BigDecimal actualHours) {
        this.actualHours = actualHours;
    }

    public BigDecimal getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(BigDecimal remainingHours) {
        this.remainingHours = remainingHours;
    }

    public java.util.List<TaskInfoResp> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(java.util.List<TaskInfoResp> subtasks) {
        this.subtasks = subtasks;
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

    public DefectSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(DefectSeverity severity) {
        this.severity = severity;
    }

    public String getReproductionSteps() {
        return reproductionSteps;
    }

    public void setReproductionSteps(String reproductionSteps) {
        this.reproductionSteps = reproductionSteps;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
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

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public Long getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
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
        resp.setProjectName(task.getProjectName());
        resp.setIterationId(task.getIterationId());
        resp.setIterationName(task.getIterationName());
        resp.setParentId(task.getParentId());
        resp.setTitle(task.getTitle());
        resp.setDescription(task.getDescription());
        resp.setType(task.getType());
        resp.setPriority(task.getPriority());
        resp.setSeverity(task.getSeverity());
        resp.setReproductionSteps(task.getReproductionSteps());
        resp.setAttachments(task.getAttachments());
        resp.setFixVersion(task.getFixVersion());
        resp.setAssigneeId(task.getAssigneeId());
        resp.setStatus(task.getStatus());
        resp.setPlanStartDate(task.getPlanStartDate());
        resp.setPlanEndDate(task.getPlanEndDate());
        resp.setActualStartDate(task.getActualStartDate());
        resp.setActualEndDate(task.getActualEndDate());
        resp.setEstimateHours(task.getEstimateHours());
        resp.setCreateUserId(task.getCreateUserId());
        resp.setUpdateUserId(task.getUpdateUserId());
        resp.setCreateTime(task.getCreateTime());
        resp.setUpdateTime(task.getUpdateTime());

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

    /**
     * 构建任务树结构
     * 将扁平的任务列表转换为树形结构，每个任务的 subtasks 字段包含其子任务
     *
     * @param tasks 扁平的任务列表
     * @return 树形结构的任务列表（只包含顶级任务）
     */
    public static java.util.List<TaskInfoResp> buildTree(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 1. 将所有任务转换为 TaskInfoResp
        java.util.List<TaskInfoResp> allTasks = from(tasks);

        // 2. 创建 ID 到任务的映射，方便快速查找
        java.util.Map<Long, TaskInfoResp> taskMap = allTasks.stream()
                .collect(Collectors.toMap(TaskInfoResp::getId, task -> task));

        // 3. 构建树结构：将子任务添加到父任务的 subtasks 列表中
        java.util.List<TaskInfoResp> rootTasks = new java.util.ArrayList<>();
        for (TaskInfoResp task : allTasks) {
            if (task.getParentId() == null) {
                // 顶级任务
                rootTasks.add(task);
            } else {
                // 子任务：添加到父任务的 subtasks 中
                TaskInfoResp parentTask = taskMap.get(task.getParentId());
                if (parentTask != null) {
                    if (parentTask.getSubtasks() == null) {
                        parentTask.setSubtasks(new java.util.ArrayList<>());
                    }
                    parentTask.getSubtasks().add(task);
                }
            }
        }

        return rootTasks;
    }
}