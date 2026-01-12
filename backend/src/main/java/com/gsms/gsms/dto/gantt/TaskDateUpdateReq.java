package com.gsms.gsms.dto.gantt;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 任务时间更新请求
 */
@Schema(description = "任务时间更新请求")
public class TaskDateUpdateReq {

    @NotNull(message = "计划开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "计划开始日期", example = "2024-01-01")
    private LocalDate planStartDate;

    @NotNull(message = "计划结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "计划结束日期", example = "2024-01-15")
    private LocalDate planEndDate;

    // Getters and Setters
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
}
