package com.gsms.gsms.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 项目成员统计信息响应DTO
 */
@Schema(description = "项目成员统计信息")
public class ProjectMemberStatsResp {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "任务总数")
    private Integer totalTasks;

    @Schema(description = "已完成任务数")
    private Integer completedTasks;

    @Schema(description = "总工时（小时）")
    private Double totalHours;

    @Schema(description = "本周工时（小时）")
    private Double weekHours;

    @Schema(description = "本月工时（小时）")
    private Double monthHours;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    public Double getWeekHours() {
        return weekHours;
    }

    public void setWeekHours(Double weekHours) {
        this.weekHours = weekHours;
    }

    public Double getMonthHours() {
        return monthHours;
    }

    public void setMonthHours(Double monthHours) {
        this.monthHours = monthHours;
    }
}
