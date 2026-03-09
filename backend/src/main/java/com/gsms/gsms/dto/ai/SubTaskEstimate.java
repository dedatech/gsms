package com.gsms.gsms.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 子任务估算
 */
public class SubTaskEstimate {

    /**
     * 子任务序号
     */
    @JsonProperty("sequence")
    private Integer sequence;

    /**
     * 子任务标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 子任务描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 预估人天（单位：人天）
     */
    @JsonProperty("estimatedDays")
    private Double estimatedDays;

    /**
     * 任务类型（如：前端开发、后端开发、测试、设计等）
     */
    @JsonProperty("taskType")
    private String taskType;

    /**
     * 优先级（高、中、低）
     */
    @JsonProperty("priority")
    private String priority;

    /**
     * 依赖任务序号（可选）
     * 注意：DeepSeek API 可能返回 null、数字或数组，使用 Object 类型接收
     */
    @JsonProperty("dependsOn")
    private Object dependsOn;

    /**
     * 备注
     */
    @JsonProperty("notes")
    private String notes;

    public SubTaskEstimate() {
    }

    public SubTaskEstimate(Integer sequence, String title, String description, Double estimatedDays) {
        this.sequence = sequence;
        this.title = title;
        this.description = description;
        this.estimatedDays = estimatedDays;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
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

    public Double getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(Double estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * 获取依赖任务序号
     * @return 依赖任务序号，如果没有依赖或为数组则返回 null
     */
    public Integer getDependsOn() {
        if (dependsOn == null) {
            return null;
        }
        if (dependsOn instanceof Number) {
            return ((Number) dependsOn).intValue();
        }
        // 如果是数组或其他类型，返回 null
        return null;
    }

    public void setDependsOn(Object dependsOn) {
        this.dependsOn = dependsOn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
