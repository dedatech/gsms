package com.gsms.gsms.controller;

import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证授权控制器
 * 提供用户权限、角色等认证相关信息查询接口
 */
@Tag(name = "认证授权", description = "用户权限、角色查询接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 获取当前用户的权限码列表
     *
     * @return 权限码列表
     */
    @Operation(summary = "获取当前用户权限码列表")
    @GetMapping("/permissions")
    public Result<List<String>> getUserPermissions() {
        Long currentUserId = UserContext.getCurrentUserId();
        logger.info("查询用户权限码列表: userId={}", currentUserId);

        List<String> permissions = authService.getPermissionCodes(currentUserId);
        logger.info("用户权限码数量: {}", permissions.size());

        return Result.success(permissions);
    }

    /**
     * 获取当前用户的角色编码列表
     *
     * @return 角色编码列表
     */
    @Operation(summary = "获取当前用户角色编码列表")
    @GetMapping("/roles")
    public Result<List<String>> getUserRoles() {
        Long currentUserId = UserContext.getCurrentUserId();
        logger.info("查询用户角色编码列表: userId={}", currentUserId);

        List<String> roles = authService.getRoleCodes(currentUserId);
        logger.info("用户角色编码: {}", roles);

        return Result.success(roles);
    }
}
