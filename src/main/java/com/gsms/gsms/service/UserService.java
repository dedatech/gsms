package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户实体
     */
    User getUserById(Long id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 创建用户
     * @param user 用户实体
     * @return 创建成功的用户实体
     * @throws BusinessException 用户名已存在时抛出异常
     */
    User createUser(User user);

    /**
     * 更新用户
     * @param user 用户实体
     * @return 更新后的用户实体
     * @throws BusinessException 用户不存在时抛出异常
     */
    User updateUser(User user);

    /**
     * 删除用户
     * @param id 用户ID
     * @throws BusinessException 用户不存在时抛出异常
     */
    void deleteUser(Long id);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户实体
     * @throws BusinessException 用户不存在或密码错误时抛出异常
     */
    User login(String username, String password);
}