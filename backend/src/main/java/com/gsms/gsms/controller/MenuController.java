package com.gsms.gsms.controller;

import com.gsms.gsms.dto.menu.*;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.MenuService;
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
 * 菜单管理控制器
 */
@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 根据ID查询菜单
     */
    @Operation(summary = "根据ID获取菜单")
    @GetMapping("/{id}")
    public Result<MenuInfoResp> getById(
            @Parameter(description = "菜单ID", required = true) @PathVariable Long id) {
        logger.info("根据ID查询菜单: {}", id);
        MenuInfoResp menu = menuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 根据条件分页查询菜单
     */
    @Operation(summary = "根据条件分页查询菜单")
    @GetMapping
    public PageResult<MenuInfoResp> findAll(@Valid MenuQueryReq req) {
        logger.info("根据条件分页查询菜单: name={}, type={}, status={}",
                    req.getName(), req.getType(), req.getStatus());
        return menuService.findAll(req);
    }

    /**
     * 查询所有菜单（树形结构）
     */
    @Operation(summary = "查询所有菜单（树形）")
    @GetMapping("/tree")
    public Result<List<MenuInfoResp>> getTree() {
        logger.info("查询所有菜单树");
        List<MenuInfoResp> tree = menuService.getTree();
        return Result.success(tree);
    }

    /**
     * 根据当前用户查询可访问菜单（树形结构）
     */
    @Operation(summary = "获取当前用户可访问菜单")
    @GetMapping("/user/tree")
    public Result<List<MenuInfoResp>> getUserMenuTree() {
        logger.info("获取当前用户菜单树");
        List<MenuInfoResp> tree = menuService.getUserMenuTree();
        return Result.success(tree);
    }

    /**
     * 创建菜单
     */
    @Operation(summary = "创建菜单")
    @PostMapping
    public Result<MenuInfoResp> create(@Validated @RequestBody MenuCreateReq req) {
        logger.info("创建菜单: {}", req.getName());
        MenuInfoResp createdMenu = menuService.create(req);
        logger.info("菜单创建成功: {}", createdMenu.getId());
        return Result.success(createdMenu);
    }

    /**
     * 更新菜单
     */
    @Operation(summary = "更新菜单")
    @PutMapping
    public Result<MenuInfoResp> update(@Validated @RequestBody MenuUpdateReq req) {
        logger.info("更新菜单: {}", req.getId());
        MenuInfoResp updatedMenu = menuService.update(req);
        logger.info("菜单更新成功: {}", updatedMenu.getId());
        return Result.success(updatedMenu);
    }

    /**
     * 删除菜单
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @Parameter(description = "菜单ID", required = true) @PathVariable Long id) {
        logger.info("删除菜单: {}", id);
        menuService.delete(id);
        logger.info("菜单删除成功: {}", id);
        return Result.success("菜单删除成功");
    }

    /**
     * 为菜单分配权限
     */
    @Operation(summary = "为菜单分配权限")
    @PostMapping("/{id}/permissions")
    public Result<String> assignPermissions(
            @Parameter(description = "菜单ID", required = true) @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        logger.info("为菜单分配权限: menuId={}, permissions={}", id, permissionIds);
        menuService.assignPermissions(id, permissionIds);
        logger.info("菜单权限分配成功: menuId={}", id);
        return Result.success("权限分配成功");
    }

    /**
     * 查询菜单的权限ID列表
     */
    @Operation(summary = "查询菜单权限列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(
            @Parameter(description = "菜单ID", required = true) @PathVariable Long id) {
        logger.info("查询菜单权限列表: menuId={}", id);
        List<Long> permissionIds = menuService.getPermissionIds(id);
        return Result.success(permissionIds);
    }
}
