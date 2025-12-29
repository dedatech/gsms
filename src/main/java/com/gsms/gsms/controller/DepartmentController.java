package com.gsms.gsms.controller;

import com.gsms.gsms.dto.department.DepartmentCreateReq;
import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.dto.department.DepartmentUpdateReq;
import com.gsms.gsms.dto.department.DepartmentInfoResp;
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

import javax.validation.Valid;

/**
 * 部门控制器
 */
@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 根据ID查询部门
     *
     * @param id 部门ID
     * @return 部门详细信息
     */
    @Operation(summary = "根据ID获取部门")
    @GetMapping("/{id}")
    public Result<DepartmentInfoResp> getById(
            @Parameter(description = "部门ID", required = true) @PathVariable Long id) {
        logger.info("根据ID查询部门: {}", id);
        DepartmentInfoResp department = departmentService.getById(id);
        logger.info("成功查询到部门: {}", department.getName());
        return Result.success(department);
    }

    /**
     * 根据条件分页查询部门
     *
     * @param req 查询条件（部门名称、上级部门ID、分页参数）
     * @return 分页结果
     */
    @Operation(summary = "根据条件分页查询部门")
    @GetMapping
    public PageResult<DepartmentInfoResp> findAll(@Valid DepartmentQueryReq req) {
        logger.info("根据条件分页查询部门: name={}, parentId={}, pageNum={}, pageSize={}",
                req.getName(), req.getParentId(), req.getPageNum(), req.getPageSize());
        return departmentService.findAll(req);
    }

    /**
     * 创建部门
     *
     * @param req 部门创建请求对象
     * @return 创建的部门信息
     */
    @Operation(summary = "创建部门")
    @PostMapping
    public Result<DepartmentInfoResp> create(@Validated @RequestBody DepartmentCreateReq req) {
        logger.info("创建部门: {}", req.getName());
        DepartmentInfoResp createdDepartment = departmentService.create(req);
        logger.info("部门创建成功: {}", createdDepartment.getId());
        return Result.success(createdDepartment);
    }

    /**
     * 更新部门
     *
     * @param req 部门更新请求对象
     * @return 更新后的部门信息
     */
    @Operation(summary = "更新部门")
    @PutMapping
    public Result<DepartmentInfoResp> update(@Validated @RequestBody DepartmentUpdateReq req) {
        logger.info("更新部门: {}", req.getId());
        DepartmentInfoResp updatedDepartment = departmentService.update(req);
        logger.info("部门更新成功: {}", updatedDepartment.getId());
        return Result.success(updatedDepartment);
    }

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
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
