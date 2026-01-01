package com.gsms.gsms.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 任务更新请求（继承公共字段 + 更新专属字段）
 */
@Schema(description = "任务更新请求")
public class TaskUpdateReq extends TaskBaseReq {
    
    @NotNull(message = "任务ID不能为空")
    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式不正确，应为 yyyy-MM-dd")
    @Schema(description = "实际开始日期（格式：yyyy-MM-dd）", example = "2025-01-02")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式不正确，应为 yyyy-MM-dd")
    @Schema(description = "实际结束日期（格式：yyyy-MM-dd）", example = "2025-01-09")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
