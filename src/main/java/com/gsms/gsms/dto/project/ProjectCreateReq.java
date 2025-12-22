package com.gsms.gsms.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 项目创建请求（继承公共字段）
 */
@Schema(description = "项目创建请求")
public class ProjectCreateReq extends ProjectBaseReq {
    // 创建时不需要额外字段，所有字段继承自 ProjectBaseReq
}
