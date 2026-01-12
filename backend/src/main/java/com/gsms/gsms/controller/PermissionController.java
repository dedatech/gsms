package com.gsms.gsms.controller;

import com.gsms.gsms.dto.permission.PermissionCreateReq;
import com.gsms.gsms.dto.permission.PermissionInfoResp;
import com.gsms.gsms.dto.permission.PermissionQueryReq;
import com.gsms.gsms.dto.permission.PermissionUpdateReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 权限控制器
 */
@Tag(name = "权限管理", description = "权限管理相关接口")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 根据ID查询权限
     *
     * @param id 权限ID
     * @return 权限详细信息
     */
    @Operation(summary = "根据ID获取权限")
    @GetMapping("/{id}")
    public Result<PermissionInfoResp> getById(
            @Parameter(description = "权限ID", required = true) @PathVariable Long id) {
        logger.info("根据ID查询权限: {}", id);
        PermissionInfoResp permission = permissionService.getById(id);
        logger.info("成功查询到权限: {}", permission.getName());
        return Result.success(permission);
    }

    /**
     * 根据条件分页查询权限
     *
     * @param req 查询条件（权限名称、权限编码、分页参数）
     * @return 分页结果
     */
    @Operation(summary = "根据条件分页查询权限")
    @GetMapping
    public PageResult<PermissionInfoResp> findAll(@Valid PermissionQueryReq req) {
        logger.info("根据条件分页查询权限: name={}, code={}, pageNum={}, pageSize={}",
                req.getName(), req.getCode(), req.getPageNum(), req.getPageSize());
        return permissionService.findAll(req);
    }

    /**
     * 获取所有权限（不分页，用于角色分配）
     *
     * @return 所有权限列表
     */
    @Operation(summary = "获取所有权限")
    @GetMapping("/all")
    public Result<List<PermissionInfoResp>> getAll() {
        logger.info("获取所有权限");
        List<PermissionInfoResp> permissions = permissionService.getAll();
        logger.info("成功查询到权限: {} 个", permissions.size());
        return Result.success(permissions);
    }

    /**
     * 创建权限
     *
     * @param req 权限创建请求对象
     * @return 创建的权限信息
     */
    @Operation(summary = "创建权限")
    @PostMapping
    public Result<PermissionInfoResp> create(@Validated @RequestBody PermissionCreateReq req) {
        logger.info("创建权限: {}", req.getName());
        PermissionInfoResp createdPermission = permissionService.create(req);
        logger.info("权限创建成功: {}", createdPermission.getId());
        return Result.success(createdPermission);
    }

    /**
     * 更新权限
     *
     * @param req 权限更新请求对象
     * @return 更新后的权限信息
     */
    @Operation(summary = "更新权限")
    @PutMapping
    public Result<PermissionInfoResp> update(@Validated @RequestBody PermissionUpdateReq req) {
        logger.info("更新权限: {}", req.getId());
        PermissionInfoResp updatedPermission = permissionService.update(req);
        logger.info("权限更新成功: {}", updatedPermission.getId());
        return Result.success(updatedPermission);
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 操作结果
     */
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @Parameter(description = "权限ID", required = true) @PathVariable Long id) {
        logger.info("删除权限: {}", id);
        permissionService.delete(id);
        logger.info("权限删除成功: {}", id);
        return Result.success("权限删除成功");
    }
}
