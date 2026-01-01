package com.gsms.gsms.dto.task;

import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.dto.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 任务查询请求
 */
@Schema(description = "任务查询请求")
public class TaskQueryReq extends BasePageQuery {
    
    @Schema(description = "项目ID", example = "1")
    private Long projectId;
    
    @Schema(description = "负责人ID", example = "1")
    private Long assigneeId;
    
    @Schema(description = "任务状态", example = "IN_PROGRESS")
    private TaskStatus status;

    // Getter and Setter
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
