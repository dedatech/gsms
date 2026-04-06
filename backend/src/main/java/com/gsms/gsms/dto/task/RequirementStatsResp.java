package com.gsms.gsms.dto.task;

import com.gsms.gsms.model.enums.TaskPriority;
import com.gsms.gsms.model.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 需求统计信息响应DTO
 */
@Schema(description = "需求统计信息")
public class RequirementStatsResp {

    @Schema(description = "需求ID")
    private Long id;

    @Schema(description = "需求标题")
    private String title;

    @Schema(description = "任务类型")
    private TaskType type;

    @Schema(description = "优先级")
    private TaskPriority priority;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人姓名")
    private String assigneeName;

    @Schema(description = "预估工时")
    private BigDecimal estimateHours;

    @Schema(description = "子任务总数")
    private Integer subtaskCount;

    @Schema(description = "已完成子任务数")
    private Integer completedSubtasks;

    @Schema(description = "待办子任务数")
    private Integer todoSubtasks;

    @Schema(description = "进行中子任务数")
    private Integer inProgressSubtasks;

    @Schema(description = "待验证子任务数（缺陷）")
    private Integer testingSubtasks;

    @Schema(description = "重新打开子任务数（缺陷）")
    private Integer reopenedSubtasks;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public BigDecimal getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(BigDecimal estimateHours) {
        this.estimateHours = estimateHours;
    }

    public Integer getSubtaskCount() {
        return subtaskCount;
    }

    public void setSubtaskCount(Integer subtaskCount) {
        this.subtaskCount = subtaskCount;
    }

    public Integer getCompletedSubtasks() {
        return completedSubtasks;
    }

    public void setCompletedSubtasks(Integer completedSubtasks) {
        this.completedSubtasks = completedSubtasks;
    }

    public Integer getTodoSubtasks() {
        return todoSubtasks;
    }

    public void setTodoSubtasks(Integer todoSubtasks) {
        this.todoSubtasks = todoSubtasks;
    }

    public Integer getInProgressSubtasks() {
        return inProgressSubtasks;
    }

    public void setInProgressSubtasks(Integer inProgressSubtasks) {
        this.inProgressSubtasks = inProgressSubtasks;
    }

    public Integer getTestingSubtasks() {
        return testingSubtasks;
    }

    public void setTestingSubtasks(Integer testingSubtasks) {
        this.testingSubtasks = testingSubtasks;
    }

    public Integer getReopenedSubtasks() {
        return reopenedSubtasks;
    }

    public void setReopenedSubtasks(Integer reopenedSubtasks) {
        this.reopenedSubtasks = reopenedSubtasks;
    }
}
