package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.Department;
import com.gsms.gsms.dto.department.DepartmentConverter;
import com.gsms.gsms.dto.department.DepartmentCreateReq;
import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.dto.department.DepartmentUpdateReq;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.github.pagehelper.PageHelper;

@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "根据ID获取部门")
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }

    @Operation(summary = "根据条件分页查询部门")
    @GetMapping
    public PageResult<Department> getDepartments(DepartmentQueryReq req) {
        // 使用默认分页参数（pageNum=1, pageSize=100）
        PageHelper.startPage(1, 100);
        List<Department> departments = departmentService.getDepartmentsByCondition(req.getName(), req.getParentId());
        return PageResult.success(departments);
    }

    @Operation(summary = "根据父部门ID获取子部门列表")
    @GetMapping("/children/{parentId}")
    public PageResult<Department> getDepartmentsByParentId(
            @Parameter(description = "父部门ID", required = true) @PathVariable Long parentId) {
        // 使用默认分页参数
        PageHelper.startPage(1, 100);
        List<Department> departments = departmentService.getDepartmentsByParentId(parentId);
        return PageResult.success(departments);
    }
    
    @Operation(summary = "根据条件分页查询部门")
    @GetMapping("/search")
    public PageResult<Department> getDepartmentsByCondition(DepartmentQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Department> departments = departmentService.getDepartmentsByCondition(req.getName(), req.getParentId());
        return PageResult.success(departments);
    }

    @Operation(summary = "创建部门")
    @PostMapping
    public Result<Department> createDepartment(@Validated @RequestBody DepartmentCreateReq req) {
        Department department = DepartmentConverter.toEntity(req);
        Department createdDepartment = departmentService.createDepartment(department);
        return Result.success(createdDepartment);
    }

    @Operation(summary = "更新部门")
    @PutMapping
    public Result<Department> updateDepartment(@Validated @RequestBody DepartmentUpdateReq req) {
        Department department = DepartmentConverter.toEntity(req);
        Department updatedDepartment = departmentService.updateDepartment(department);
        return Result.success(updatedDepartment);
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }
}
