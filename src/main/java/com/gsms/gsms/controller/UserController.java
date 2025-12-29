package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.user.UserLoginReq;
import com.gsms.gsms.dto.user.UserQueryReq;
import com.gsms.gsms.dto.user.UserRegisterReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserUpdateReq;
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

    public UserController(UserService userService) {
        this.userService = userService;
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
}
