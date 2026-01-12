package com.gsms.gsms.dto.gantt;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 甘特图数据响应
 */
@Schema(description = "甘特图数据响应")
public class GanttDataResp {

    @Schema(description = "任务列表（树形结构）")
    private List<GanttTaskResp> data;

    @Schema(description = "任务依赖关系列表")
    private List<GanttLinkResp> links;

    public List<GanttTaskResp> getData() {
        return data;
    }

    public void setData(List<GanttTaskResp> data) {
        this.data = data;
    }

    public List<GanttLinkResp> getLinks() {
        return links;
    }

    public void setLinks(List<GanttLinkResp> links) {
        this.links = links;
    }
}
