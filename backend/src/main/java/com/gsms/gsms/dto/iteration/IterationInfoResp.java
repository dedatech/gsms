package com.gsms.gsms.dto.iteration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.Iteration;
import com.gsms.gsms.model.enums.IterationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 迭代信息响应
 */
@Schema(description = "迭代信息响应")
public class IterationInfoResp {

    @Schema(description = "迭代ID")
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "迭代名称")
    private String name;

    @Schema(description = "迭代描述")
    private String description;

    @Schema(description = "迭代状态")
    private IterationStatus status;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planStartDate;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planEndDate;

    @Schema(description = "实际开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @Schema(description = "实际结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDate getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(LocalDate planStartDate) {
        this.planStartDate = planStartDate;
    }

    public LocalDate getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(LocalDate planEndDate) {
        this.planEndDate = planEndDate;
    }

    public LocalDate getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(LocalDate actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 将 Iteration 实体转换为 IterationInfoResp
     */
    public static IterationInfoResp from(Iteration iteration) {
        if (iteration == null) {
            return null;
        }

        IterationInfoResp resp = new IterationInfoResp();
        resp.setId(iteration.getId());
        resp.setProjectId(iteration.getProjectId());
        resp.setName(iteration.getName());
        resp.setDescription(iteration.getDescription());
        resp.setStatus(iteration.getStatus());
        resp.setPlanStartDate(iteration.getPlanStartDate());
        resp.setPlanEndDate(iteration.getPlanEndDate());
        resp.setActualStartDate(iteration.getActualStartDate());
        resp.setActualEndDate(iteration.getActualEndDate());
        resp.setCreateUserId(iteration.getCreateUserId());
        resp.setCreateTime(iteration.getCreateTime());
        resp.setUpdateTime(iteration.getUpdateTime());

        return resp;
    }

    /**
     * 将 Iteration 列表转换为 IterationInfoResp 列表
     */
    public static List<IterationInfoResp> from(List<Iteration> iterations) {
        if (iterations == null) {
            return java.util.Collections.emptyList();
        }

        return iterations.stream()
                .map(IterationInfoResp::from)
                .collect(Collectors.toList());
    }
}
