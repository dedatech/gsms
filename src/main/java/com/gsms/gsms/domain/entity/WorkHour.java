package com.gsms.gsms.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 工时记录实体类
 */
@Schema(description = "工时记录信息")
public class WorkHour {
    /**
     * 工时记录ID
     */
    @Schema(description = "工时记录ID")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private Long taskId;

    /**
     * 工作日期
     */
    @Schema(description = "工作日期")
    private Date workDate;

    /**
     * 工时数
     */
    @Schema(description = "工时数")
    private BigDecimal hours;

    /**
     * 工作内容描述
     */
    @Schema(description = "工作内容描述")
    private String content;

    /**
     * 状态 1:已保存 2:已提交 3:已确认
     */
    @Schema(description = "状态 1:已保存 2:已提交 3:已确认")
    private Integer status;

    /**
     * 更新人id
     */
    @Schema(description = "更新人id")
    private Long updateUserId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}