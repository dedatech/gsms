package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.Department;
import com.gsms.gsms.dto.department.DepartmentConverter;
import com.gsms.gsms.dto.department.DepartmentCreateReq;
import com.gsms.gsms.dto.department.DepartmentUpdateReq;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Operation(summary = "根据ID获取部门")
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }

    @Operation(summary = "获取所有部门")
    @GetMapping
    public Result<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return Result.success(departments);
    }

    @Operation(summary = "根据父部门ID获取子部门列表")
    @GetMapping("/children/{parentId}")
    public Result<List<Department>> getDepartmentsByParentId(
            @Parameter(description = "父部门ID", required = true) @PathVariable Long parentId) {
        List<Department> departments = departmentService.getDepartmentsByParentId(parentId);
        return Result.success(departments);
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
