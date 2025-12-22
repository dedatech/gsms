package com.gsms.gsms.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 任务更新请求（继承公共字段 + 更新专属字段）
 */
@Schema(description = "任务更新请求")
public class TaskUpdateReq extends TaskBaseReq {
    
    @NotNull(message = "任务ID不能为空")
    @Schema(description = "任务ID", required = true, example = "1")
    private Long id;
    
    @Schema(description = "实际开始日期", example = "2025-01-02")
    private Date actualStartDate;
    
    @Schema(description = "实际结束日期", example = "2025-01-09")
    private Date actualEndDate;

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
