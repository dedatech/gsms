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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "根据ID获取部门")
    @GetMapping("/{id}")
    public Result<Department> getById(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        logger.info("根据ID查询部门: {}", id);
        Department department = departmentService.getById(id);
        logger.info("成功查询到部门: {}", department.getName());
        return Result.success(department);
    }

    @Operation(summary = "根据条件分页查询部门")
    @GetMapping
    public PageResult<Department> findAll(DepartmentQueryReq req) {
        logger.info("根据条件分页查询部门: name={}, parentId={}, pageNum={}, pageSize={}",
                req.getName(), req.getParentId(), req.getPageNum(), req.getPageSize());
        return departmentService.findAll(req);
    }

    @Operation(summary = "创建部门")
    @PostMapping
    public Result<Department> create(@Validated @RequestBody DepartmentCreateReq req) {
        logger.info("创建部门: {}", req.getName());
        Department department = DepartmentConverter.toEntity(req);
        Department createdDepartment = departmentService.create(department);
        logger.info("部门创建成功: {}", createdDepartment.getId());
        return Result.success(createdDepartment);
    }

    @Operation(summary = "更新部门")
    @PutMapping
    public Result<Department> update(@Validated @RequestBody DepartmentUpdateReq req) {
        logger.info("更新部门: {}", req.getId());
        Department department = DepartmentConverter.toEntity(req);
        Department updatedDepartment = departmentService.update(department);
        logger.info("部门更新成功: {}", updatedDepartment.getId());
        return Result.success(updatedDepartment);
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        logger.info("删除部门: {}", id);
        departmentService.delete(id);
        logger.info("部门删除成功: {}", id);
        return Result.success("部门删除成功");
    }
}
