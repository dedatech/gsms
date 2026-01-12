package com.gsms.gsms.controller;

import com.gsms.gsms.dto.role.RoleCreateReq;
import com.gsms.gsms.dto.role.RoleInfoResp;
import com.gsms.gsms.dto.role.RolePermissionAssignReq;
import com.gsms.gsms.dto.role.RoleQueryReq;
import com.gsms.gsms.dto.role.RoleUpdateReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.RoleService;
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
 * 角色控制器
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色详细信息
     */
    @Operation(summary = "根据ID获取角色")
    @GetMapping("/{id}")
    public Result<RoleInfoResp> getById(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        logger.info("根据ID查询角色: {}", id);
        RoleInfoResp role = roleService.getById(id);
        logger.info("成功查询到角色: {}", role.getName());
        return Result.success(role);
    }

    /**
     * 根据条件分页查询角色
     *
     * @param req 查询条件（角色名称、角色编码、角色级别、分页参数）
     * @return 分页结果
     */
    @Operation(summary = "根据条件分页查询角色")
    @GetMapping
    public PageResult<RoleInfoResp> findAll(@Valid RoleQueryReq req) {
        logger.info("根据条件分页查询角色: name={}, code={}, roleLevel={}, pageNum={}, pageSize={}",
                req.getName(), req.getCode(), req.getRoleLevel(), req.getPageNum(), req.getPageSize());
        return roleService.findAll(req);
    }

    /**
     * 创建角色
     *
     * @param req 角色创建请求对象
     * @return 创建的角色信息
     */
    @Operation(summary = "创建角色")
    @PostMapping
    public Result<RoleInfoResp> create(@Validated @RequestBody RoleCreateReq req) {
        logger.info("创建角色: {}", req.getName());
        RoleInfoResp createdRole = roleService.create(req);
        logger.info("角色创建成功: {}", createdRole.getId());
        return Result.success(createdRole);
    }

    /**
     * 更新角色
     *
     * @param req 角色更新请求对象
     * @return 更新后的角色信息
     */
    @Operation(summary = "更新角色")
    @PutMapping
    public Result<RoleInfoResp> update(@Validated @RequestBody RoleUpdateReq req) {
        logger.info("更新角色: {}", req.getId());
        RoleInfoResp updatedRole = roleService.update(req);
        logger.info("角色更新成功: {}", updatedRole.getId());
        return Result.success(updatedRole);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        logger.info("删除角色: {}", id);
        roleService.delete(id);
        logger.info("角色删除成功: {}", id);
        return Result.success("角色删除成功");
    }

    /**
     * 查询角色的权限ID列表
     *
     * @param id 角色ID
     * @return 权限ID列表
     */
    @Operation(summary = "查询角色权限列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        logger.info("查询角色权限列表: roleId={}", id);
        List<Long> permissionIds = roleService.getPermissionIds(id);
        logger.info("成功查询到角色权限: {} 个", permissionIds.size());
        return Result.success(permissionIds);
    }

    /**
     * 为角色分配权限
     *
     * @param req 角色权限分配请求对象
     * @return 操作结果
     */
    @Operation(summary = "为角色分配权限")
    @PostMapping("/{id}/permissions")
    public Result<String> assignPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @Validated @RequestBody RolePermissionAssignReq req) {
        logger.info("为角色分配权限: roleId={}, permissions={}", id, req.getPermissionIds());
        req.setRoleId(id);
        roleService.assignPermissions(req);
        logger.info("角色权限分配成功: roleId={}", id);
        return Result.success("权限分配成功");
    }

    /**
     * 移除角色的权限
     *
     * @param roleId          角色ID
     * @param permissionId 权限ID
     * @return 操作结果
     */
    @Operation(summary = "移除角色权限")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public Result<String> removePermission(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Parameter(description = "权限ID", required = true) @PathVariable Long permissionId) {
        logger.info("移除角色权限: roleId={}, permissionId={}", roleId, permissionId);
        roleService.removePermission(roleId, permissionId);
        logger.info("角色权限移除成功");
        return Result.success("权限移除成功");
    }

    /**
     * 查询拥有该角色的用户列表
     *
     * @param id 角色ID
     * @return 用户ID列表
     */
    @Operation(summary = "查询拥有该角色的用户列表")
    @GetMapping("/{id}/users")
    public Result<List<Long>> getUsers(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        logger.info("查询拥有该角色的用户列表: roleId={}", id);
        List<Long> userIds = roleService.getUserIds(id);
        logger.info("成功查询到用户: {} 个", userIds.size());
        return Result.success(userIds);
    }
}
