package com.gsms.gsms.service.impl;

import com.gsms.gsms.entity.User;
import com.gsms.gsms.mapper.UserMapper;
import com.gsms.gsms.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Long id) {
        logger.debug("根据ID查询用户: {}", id);
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        logger.debug("根据用户名查询用户: {}", username);
        return userMapper.selectByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("查询所有用户");
        return userMapper.selectAll();
    }

    @Override
    public boolean createUser(User user) {
        logger.info("创建用户: {}", user.getUsername());
        int result = userMapper.insert(user);
        boolean success = result > 0;
        if (success) {
            logger.info("用户创建成功: {}", user.getUsername());
        } else {
            logger.error("用户创建失败: {}", user.getUsername());
        }
        return success;
    }

    @Override
    public boolean updateUser(User user) {
        logger.info("更新用户: {}", user.getId());
        int result = userMapper.update(user);
        boolean success = result > 0;
        if (success) {
            logger.info("用户更新成功: {}", user.getId());
        } else {
            logger.error("用户更新失败: {}", user.getId());
        }
        return success;
    }

    @Override
    public boolean deleteUser(Long id) {
        logger.info("删除用户: {}", id);
        int result = userMapper.deleteById(id);
        boolean success = result > 0;
        if (success) {
            logger.info("用户删除成功: {}", id);
        } else {
            logger.error("用户删除失败: {}", id);
        }
        return success;
    }

    @Override
    public User login(String username, String password) {
        logger.info("用户登录: {}", username);
        User user = userMapper.selectByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            logger.info("用户登录成功: {}", username);
            return user;
        }
        logger.warn("用户登录失败: {}", username);
        return null;
    }
}