package com.gsms.gsms.dto.workhour;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 工时记录查询请求
 */
@Schema(description = "工时记录查询请求")
public class WorkHourQueryReq {
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @Schema(description = "项目ID", example = "1")
    private Long projectId;
    
    @Schema(description = "任务ID", example = "1")
    private Long taskId;
    
    @Schema(description = "开始日期", example = "2025-01-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    
    @Schema(description = "结束日期", example = "2025-01-31")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    // Getter and Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}