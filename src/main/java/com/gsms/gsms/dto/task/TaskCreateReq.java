package com.gsms.gsms.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 任务创建请求（继承公共字段）
 */
@Schema(description = "任务创建请求")
public class TaskCreateReq extends TaskBaseReq {
    // 创建时不需要额外字段，所有字段继承自 TaskBaseReq
}
