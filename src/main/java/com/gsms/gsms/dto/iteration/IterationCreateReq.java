package com.gsms.gsms.dto.iteration;

import com.gsms.gsms.model.enums.IterationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Schema(description = "创建迭代请求")
public class IterationCreateReq {

    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID")
    private Long projectId;

    @NotBlank(message = "迭代名称不能为空")
    @Schema(description = "迭代名称")
    private String name;

    @Schema(description = "迭代描述")
    private String description;

    @Schema(description = "迭代状态")
    private IterationStatus status = IterationStatus.NOT_STARTED;

    @Schema(description = "计划开始日期")
    private Date planStartDate;

    @Schema(description = "计划结束日期")
    private Date planEndDate;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IterationStatus getStatus() {
        return status;
    }

    public void setStatus(IterationStatus status) {
        this.status = status;
    }

    public Date getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(Date planStartDate) {
        this.planStartDate = planStartDate;
    }

    public Date getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(Date planEndDate) {
        this.planEndDate = planEndDate;
    }
}
