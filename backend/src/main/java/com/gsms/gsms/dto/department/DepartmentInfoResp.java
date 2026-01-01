package com.gsms.gsms.dto.department;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门信息响应
 */
@Schema(description = "部门信息响应")
public class DepartmentInfoResp {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
     * 将 Department 实体转换为 DepartmentInfoResp
     */
    public static DepartmentInfoResp from(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentInfoResp resp = new DepartmentInfoResp();
        resp.setId(department.getId());
        resp.setName(department.getName());
        resp.setParentId(department.getParentId());
        resp.setLevel(department.getLevel());
        resp.setSort(department.getSort());
        resp.setRemark(department.getRemark());
        resp.setCreateTime(department.getCreateTime());
        resp.setUpdateTime(department.getUpdateTime());

        return resp;
    }

    /**
     *将 Department 列表转换为 DepartmentInfoResp 列表
     */
    public static List<DepartmentInfoResp> from(List<Department> departments) {
        if (departments == null) {
            return java.util.Collections.emptyList();
        }

        return departments.stream()
                .map(DepartmentInfoResp::from)
                .collect(Collectors.toList());
    }
}
