package com.gsms.gsms.dto.iteration;

import com.gsms.gsms.model.enums.IterationStatus;
import com.gsms.gsms.dto.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "查询迭代请求")
public class IterationQueryReq extends BasePageQuery {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "迭代状态")
    private IterationStatus status;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public IterationStatus getStatus() {
        return status;
    }

    public void setStatus(IterationStatus status) {
        this.status = status;
    }
}