package com.gsms.gsms.dto.workhour;

import com.gsms.gsms.model.enums.WorkHourStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 工时记录创建请求
 */
@Schema(description = "工时记录创建请求")
public class WorkHourCreateReq {
    
    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID", example = "1")
    private Long projectId;
    
    @Schema(description = "任务ID（可选）",  example = "1")
    private Long taskId;
    
    @NotNull(message = "工作日期不能为空")
    @Schema(description = "工作日期", example = "2025-01-01")
    private Date workDate;
    
    @NotNull(message = "工时数不能为空")
    @Schema(description = "工时数", example = "8.0")
    private BigDecimal hours;
    
    @Schema(description = "工作内容描述", example = "完成用户登录功能开发")
    private String content;
    
    @Schema(description = "状态", example = "SAVED")
    private WorkHourStatus status;

    // Getter and Setter
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

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public WorkHourStatus getStatus() {
        return status;
    }

    public void setStatus(WorkHourStatus status) {
        this.status = status;
    }
}
