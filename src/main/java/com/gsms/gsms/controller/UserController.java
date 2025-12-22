package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.UserStatus;
import com.gsms.gsms.dto.user.UserConverter;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.user.UserLoginReq;
import com.gsms.gsms.dto.user.UserRegisterReq;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public Result<UserInfoResp> getUserById(@PathVariable Long id) {
        logger.info("根据ID查询用户: {}", id);
        User user = userService.getUserById(id);
        UserInfoResp resp = UserConverter.toUserInfoResp(user);
        logger.info("成功查询到用户: {}", user.getUsername());
        return Result.success(resp);
    }

    /**
     * 查询所有用户
     */
    @GetMapping
    @Operation(summary = "查询所有用户")
    public Result<List<UserInfoResp>> getAllUsers() {
        logger.info("查询所有用户");
        List<User> users = userService.getAllUsers();
        List<UserInfoResp> respList = users.stream()
                .map(UserConverter::toUserInfoResp)
                .collect(java.util.stream.Collectors.toList());
        logger.info("成功查询到{}个用户", users.size());
        return Result.success(respList);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<UserInfoResp> createUser(@RequestBody @Valid User user) {
        logger.info("创建用户: {}", user.getUsername());
        User createdUser = userService.createUser(user);
        UserInfoResp resp = UserConverter.toUserInfoResp(createdUser);
        logger.info("用户创建成功: {}", createdUser.getUsername());
        return Result.success(resp);
    }

    /**
     * 更新用户
     */
    @PutMapping
    @Operation(summary = "更新用户")
    public Result<UserInfoResp> updateUser(@RequestBody @Valid User user) {
        logger.info("更新用户: {}", user.getId());
        User updatedUser = userService.updateUser(user);
        UserInfoResp resp = UserConverter.toUserInfoResp(updatedUser);
        logger.info("用户更新成功: {}", updatedUser.getId());
        return Result.success(resp);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<String> deleteUser(@PathVariable Long id) {
        logger.info("删除用户: {}", id);
        userService.deleteUser(id);
        logger.info("用户删除成功: {}", id);
        return Result.success("用户删除成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<String> login(@Valid @RequestBody UserLoginReq req) {
        logger.info("用户登录: {}", req.getUsername());
        User user = userService.login(req.getUsername(), req.getPassword());
        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
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
        
        // 使用转换工具将 DTO 转为 Entity
        User user = UserConverter.toUser(req);
        user.setStatus(UserStatus.NORMAL); // 默认状态为正常
        
        User createdUser = userService.createUser(user);
        UserInfoResp resp = UserConverter.toUserInfoResp(createdUser);
        logger.info("用户注册成功: {}", createdUser.getUsername());
        return Result.success(resp);
    }
}