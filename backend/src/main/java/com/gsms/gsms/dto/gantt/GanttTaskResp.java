package com.gsms.gsms.dto.gantt;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 甘特图任务响应
 */
@Schema(description = "甘特图任务响应")
public class GanttTaskResp {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务名称")
    private String text;

    @Schema(description = "任务类型")
    private String type;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "工期（天）")
    private Integer duration;

    @Schema(description = "进度（0-1）")
    private Double progress;

    @Schema(description = "父任务ID")
    private Long parent;

    @Schema(description = "执行人姓名")
    private String owner;

    @Schema(description = "执行人ID")
    private Long ownerId;

    @Schema(description = "执行人头像URL")
    private String ownerAvatar;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "实际开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @Schema(description = "实际结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    @Schema(description = "是否关键路径任务")
    private Boolean critical;

    @Schema(description = "松弛时间（天）")
    private Integer slack;

    @Schema(description = "子任务列表")
    private List<GanttTaskResp> subtasks;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerAvatar() {
        return ownerAvatar;
    }

    public void setOwnerAvatar(String ownerAvatar) {
        this.ownerAvatar = ownerAvatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public Boolean getCritical() {
        return critical;
    }

    public void setCritical(Boolean critical) {
        this.critical = critical;
    }

    public Integer getSlack() {
        return slack;
    }

    public void setSlack(Integer slack) {
        this.slack = slack;
    }

    public List<GanttTaskResp> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<GanttTaskResp> subtasks) {
        this.subtasks = subtasks;
    }
}
