package com.gsms.gsms.dto.gantt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 甘特图数据响应
 */
@Data
@Schema(description = "甘特图数据响应")
public class GanttDataResp {

    @Schema(description = "任务列表（树形结构）")
    private List<GanttTaskResp> data;

    @Schema(description = "任务依赖关系列表")
    private List<GanttLinkResp> links;
}
