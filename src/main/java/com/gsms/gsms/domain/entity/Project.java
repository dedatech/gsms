package com.gsms.gsms.domain.entity;

import com.gsms.gsms.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

/**
 * 项目实体类
 */
@Schema(description = "项目信息")
public class Project {
    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String name;

    /**
     * 项目编码
     */
    @Schema(description = "项目编码")
    private String code;

    /**
     * 项目描述
     */
    @Schema(description = "项目描述")
    private String description;

    /**
     * 项目负责人ID
     */
    @Schema(description = "项目负责人ID")
    private Long managerId;

    /**
     * 项目状态
     */
    @Schema(description = "项目状态")
    private ProjectStatus status;

    /**
     * 计划开始日期
     */
    @Schema(description = "计划开始日期")
    private Date planStartDate;

    /**
     * 计划结束日期
     */
    @Schema(description = "计划结束日期")
    private Date planEndDate;

    /**
     * 实际开始日期
     */
    @Schema(description = "实际开始日期")
    private Date actualStartDate;

    /**
     * 实际结束日期
     */
    @Schema(description = "实际结束日期")
    private Date actualEndDate;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createUserId;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Long updateUserId;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;
    
    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    private Integer isDeleted;
    
    // Getter和Setter方法
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
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

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
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
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}