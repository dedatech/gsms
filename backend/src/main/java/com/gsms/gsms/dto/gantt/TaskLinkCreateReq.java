package com.gsms.gsms.dto.gantt;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

/**
 * 任务依赖关系创建请求
 */
@Schema(description = "任务依赖关系创建请求")
public class TaskLinkCreateReq {

    @NotNull(message = "源任务ID不能为空")
    @Schema(description = "源任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long source;

    @NotNull(message = "目标任务ID不能为空")
    @Schema(description = "目标任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long target;

    @Schema(description = "依赖类型：0-结束到开始(end_to_start)，1-开始到开始(start_to_start)，2-结束到结束(end_to_end)，3-开始到结束(start_to_end)", example = "0")
    private Integer type;

    @Schema(description = "延迟时间（毫秒）", example = "0")
    private Integer lag;

    // Getters and Setters
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLag() {
        return lag;
    }

    public void setLag(Integer lag) {
        this.lag = lag;
    }
}
