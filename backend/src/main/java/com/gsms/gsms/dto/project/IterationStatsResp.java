package com.gsms.gsms.dto.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.enums.IterationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 迭代统计信息响应DTO
 */
@Schema(description = "迭代统计信息")
public class IterationStatsResp {

    @Schema(description = "迭代ID")
    private Long iterationId;

    @Schema(description = "迭代名称")
    private String iterationName;

    @Schema(description = "迭代状态")
    private IterationStatus iterationStatus;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

    @Schema(description = "任务总数")
    private Integer totalTasks;

    @Schema(description = "已完成任务数")
    private Integer completedTasks;

    @Schema(description = "总预估工时")
    private Double totalEstimatedHours;

    @Schema(description = "已完成工时")
    private Double completedHours;

    @Schema(description = "完成进度百分比")
    private Integer progressPercent;

    // Getters and Setters
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

    public IterationStatus getIterationStatus() {
        return iterationStatus;
    }

    public void setIterationStatus(IterationStatus iterationStatus) {
        this.iterationStatus = iterationStatus;
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

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Integer getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(Integer completedTasks) {
        this.completedTasks = completedTasks;
    }

    public Double getTotalEstimatedHours() {
        return totalEstimatedHours;
    }

    public void setTotalEstimatedHours(Double totalEstimatedHours) {
        this.totalEstimatedHours = totalEstimatedHours;
    }

    public Double getCompletedHours() {
        return completedHours;
    }

    public void setCompletedHours(Double completedHours) {
        this.completedHours = completedHours;
    }

    public Integer getProgressPercent() {
        if (totalTasks == null || totalTasks == 0) {
            return 0;
        }
        if (completedTasks == null) {
            return 0;
        }
        return (int) Math.round((double) completedTasks / totalTasks * 100);
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }
}
