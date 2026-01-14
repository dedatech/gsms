package com.gsms.gsms.controller;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.user.UserLoginReq;
import com.gsms.gsms.dto.user.UserQueryReq;
import com.gsms.gsms.dto.user.UserRegisterReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserUpdateReq;
import com.gsms.gsms.dto.user.PasswordChangeReq;
import com.gsms.gsms.dto.user.PasswordResetReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理接口", description = "用户相关的API接口")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;  // 新增注入

    public UserController(UserService userService, JwtUtil jwtUtil) {  // 修改构造函数
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<UserInfoResp> getById(@PathVariable Long id) {
        logger.info("查询用户: {}", id);
        return Result.success(userService.getById(id));
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping
    @Operation(summary = "分页查询用户列表")
    public PageResult<UserInfoResp> findAll(UserQueryReq userQueryReq) {
        logger.info("查询用户列表: {}", userQueryReq);
        return userService.findAll(userQueryReq);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<UserInfoResp> create(@Valid @RequestBody UserCreateReq createReq) {
        logger.info("创建用户: {}", createReq.getUsername());
        UserInfoResp resp = userService.create(createReq);
        return Result.success(resp);
    }

    /**
     * 更新用户
     */
    @PutMapping
    @Operation(summary = "更新用户")
    public Result<UserInfoResp> update(@Valid @RequestBody UserUpdateReq updateReq) {
        logger.info("更新用户: {}", updateReq.getId());
        UserInfoResp resp = userService.update(updateReq);
        return Result.success(resp);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<String> delete(@PathVariable Long id) {
        logger.info("删除用户: {}", id);
        userService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<String> login(@Valid @RequestBody UserLoginReq req) {
        logger.info("用户登录: {}", req.getUsername());
        User user = userService.login(req.getUsername(), req.getPassword());
        // 使用注入的JwtUtil实例生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        logger.info("用户登录成功: {}", user.getUsername());
        return Result.success("登录成功", token);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserInfoResp> register(@Valid @RequestBody UserRegisterReq req) {
        logger.info("用户注册: {}", req.getUsername());

        // 将注册请求转换为创建请求
        UserCreateReq createReq = new UserCreateReq();
        createReq.setUsername(req.getUsername());
        createReq.setPassword(req.getPassword());
        createReq.setNickname(req.getNickname());
        createReq.setEmail(req.getEmail());
        createReq.setPhone(req.getPhone());

        UserInfoResp resp = userService.create(createReq);
        logger.info("用户注册成功: {}", resp.getUsername());
        return Result.success(resp);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<String> changePassword(@Valid @RequestBody PasswordChangeReq req) {
        logger.info("用户修改密码");
        userService.changePassword(req);
        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 重置密码（管理员）
     */
    @PutMapping("/password/reset")
    @Operation(summary = "重置密码（管理员）")
    public Result<String> resetPassword(@Valid @RequestBody PasswordResetReq req) {
        logger.info("管理员重置密码: userId={}", req.getUserId());
        userService.resetPassword(req);
        return Result.success("密码重置成功");
    }

    /**
     * 查询用户的角色ID列表
     */
    @GetMapping("/{id}/roles")
    @Operation(summary = "查询用户的角色列表")
    public Result<java.util.List<Long>> getRoles(@PathVariable Long id) {
        logger.info("查询用户角色列表: userId={}", id);
        return Result.success(userService.getRoleIds(id));
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/{id}/roles")
    @Operation(summary = "为用户分配角色")
    public Result<String> assignRoles(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> payload) {
        logger.info("为用户分配角色: userId={}", id);
        @SuppressWarnings("unchecked")
        java.util.List<Long> roleIds = (java.util.List<Long>) payload.get("roleIds");
        userService.assignRoles(id, roleIds);
        return Result.success("角色分配成功");
    }

    /**
     * 移除用户角色
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "移除用户角色")
    public Result<String> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        logger.info("移除用户角色: userId={}, roleId={}", userId, roleId);
        userService.removeRole(userId, roleId);
        return Result.success("角色移除成功");
    }

    /**
     * 查询用户的权限编码列表
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "查询用户的权限编码列表")
    public Result<java.util.List<String>> getPermissions(@PathVariable Long id) {
        logger.info("查询用户权限编码列表: userId={}", id);
        return Result.success(userService.getPermissionCodes(id));
    }
}
