package com.gsms.gsms.controller;

import com.gsms.gsms.common.Result;
import com.gsms.gsms.entity.User;
import com.gsms.gsms.service.UserService;
import com.gsms.gsms.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理接口", description = "用户相关的API接口")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<User> getUserById(
            @Parameter(description = "用户ID")
            @PathVariable Long id) {
        logger.info("根据ID查询用户: {}", id);
        User user = userService.getUserById(id);
        if (user != null) {
            logger.info("成功查询到用户: {}", user.getUsername());
            return Result.success(user);
        } else {
            logger.warn("未找到ID为{}的用户", id);
            return Result.error("用户不存在");
        }
    }

    /**
     * 查询所有用户
     */
    @GetMapping
    @Operation(summary = "查询所有用户")
    public Result<List<User>> getAllUsers() {
        logger.info("查询所有用户");
        List<User> users = userService.getAllUsers();
        logger.info("成功查询到{}个用户", users.size());
        return Result.success(users);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<String> createUser(
            @Parameter(description = "用户信息")
            @RequestBody User user) {
        logger.info("创建用户: {}", user.getUsername());
        boolean success = userService.createUser(user);
        if (success) {
            logger.info("用户创建成功: {}", user.getUsername());
            return Result.success("用户创建成功");
        } else {
            logger.error("用户创建失败: {}", user.getUsername());
            return Result.error("用户创建失败");
        }
    }

    /**
     * 更新用户
     */
    @PutMapping
    @Operation(summary = "更新用户")
    public Result<String> updateUser(
            @Parameter(description = "用户信息")
            @RequestBody User user) {
        logger.info("更新用户: {}", user.getId());
        boolean success = userService.updateUser(user);
        if (success) {
            logger.info("用户更新成功: {}", user.getId());
            return Result.success("用户更新成功");
        } else {
            logger.error("用户更新失败: {}", user.getId());
            return Result.error("用户更新失败");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<String> deleteUser(
            @Parameter(description = "用户ID")
            @PathVariable Long id) {
        logger.info("删除用户: {}", id);
        boolean success = userService.deleteUser(id);
        if (success) {
            logger.info("用户删除成功: {}", id);
            return Result.success("用户删除成功");
        } else {
            logger.error("用户删除失败: {}", id);
            return Result.error("用户删除失败");
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<String> login(
            @Parameter(description = "登录请求参数")
            @RequestBody LoginRequest loginRequest) {
        logger.info("用户登录: {}", loginRequest.getUsername());
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            // 生成JWT Token
            String token = JwtUtil.generateToken(user.getId(), user.getUsername());
            logger.info("用户登录成功: {}", user.getUsername());
            return Result.success("登录成功", token);
        } else {
            logger.warn("用户登录失败: {}", loginRequest.getUsername());
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 登录请求体
     */
    @Schema(description = "用户登录请求体")
    public static class LoginRequest {
        @Schema(description = "用户名")
        private String username;
        @Schema(description = "密码")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}