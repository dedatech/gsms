package com.gsms.gsms.dto.user;

import com.gsms.gsms.model.entity.User;

/**
 * 用户对象转换器（手动转换，性能最优）
 */
public class UserConverter {

    /**
     * 注册请求转用户实体
     */
    public static User toUser(UserRegisterReq req) {
        if (req == null) {
            return null;
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        return user;
    }

    /**
     * 创建请求转用户实体
     */
    public static User toUser(UserCreateReq req) {
        if (req == null) {
            return null;
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setDepartmentId(req.getDepartmentId());
        user.setStatus(req.getStatus());
        return user;
    }
    /**
     * 创建请求转用户实体
     */
    public static User toUser(UserQueryReq req) {
        if (req == null) {
            return null;
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setStatus(req.getStatus());
        return user;
    }
    /**
     * 更新请求转用户实体
     */
    public static User toUser(UserUpdateReq req) {
        if (req == null) {
            return null;
        }
        User user = new User();
        user.setId(req.getId());
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setDepartmentId(req.getDepartmentId());
        user.setStatus(req.getStatus());
        return user;
    }

    /**
     * 用户实体转用户信息响应（自动过滤密码等敏感信息）
     */
    public static UserInfoResp toUserInfoResp(User user) {
        if (user == null) {
            return null;
        }
        UserInfoResp resp = new UserInfoResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        // 注意：不设置 password，自动过滤敏感信息
        return resp;
    }
}

