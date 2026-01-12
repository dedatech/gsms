package com.gsms.gsms.dto.gantt;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 甘特图任务依赖关系响应
 */
@Schema(description = "甘特图任务依赖关系响应")
public class GanttLinkResp {

    @Schema(description = "依赖关系ID")
    private Long id;

    @Schema(description = "源任务ID")
    private Long source;

    @Schema(description = "目标任务ID")
    private Long target;

    @Schema(description = "依赖类型")
    private String type;

    @Schema(description = "延迟时间（毫秒）")
    private Integer lag;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    public Long getTarget() {
        return target;
    }

    public void setTarget(Long target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLag() {
        return lag;
    }

    public void setLag(Integer lag) {
        this.lag = lag;
    }
}
