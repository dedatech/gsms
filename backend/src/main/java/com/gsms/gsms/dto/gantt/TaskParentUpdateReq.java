package com.gsms.gsms.dto.gantt;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 任务层级更新请求
 */
@Schema(description = "任务层级更新请求")
public class TaskParentUpdateReq {

    @Schema(description = "新的父任务ID（null表示顶级任务）")
    private Long parentId;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
