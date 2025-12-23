package com.gsms.gsms.dto.iteration;

import com.gsms.gsms.domain.enums.IterationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Schema(description = "更新迭代请求")
public class IterationUpdateReq {

    @NotNull(message = "迭代ID不能为空")
    @Schema(description = "迭代ID", required = true)
    private Long id;

    @Schema(description = "迭代名称")
    private String name;

    @Schema(description = "迭代描述")
    private String description;

    @Schema(description = "迭代状态")
    private IterationStatus status;

    @Schema(description = "计划开始日期")
    private Date planStartDate;

    @Schema(description = "计划结束日期")
    private Date planEndDate;

    @Schema(description = "实际开始日期")
    private Date actualStartDate;

    @Schema(description = "实际结束日期")
    private Date actualEndDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }
}
