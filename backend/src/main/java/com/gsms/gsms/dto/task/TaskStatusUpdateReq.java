package com.gsms.gsms.dto.task;

import com.gsms.gsms.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 任务状态更新请求（轻量级，仅用于状态变更场景）
 */
@Schema(description = "任务状态更新请求")
public class TaskStatusUpdateReq {

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "任务ID", example = "1")
    private Long id;

    @NotNull(message = "任务状态不能为空")
    @Schema(description = "任务状态", example = "IN_PROGRESS")
    private TaskStatus status;

    @Schema(description = "实际开始日期（格式：yyyy-MM-dd，可选）", example = "2025-01-02")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @Schema(description = "实际结束日期（格式：yyyy-MM-dd，可选）", example = "2025-01-09")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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
}
