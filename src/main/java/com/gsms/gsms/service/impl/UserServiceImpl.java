package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.errorcode.UserErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.PasswordUtil;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        logger.debug("根据用户名查询用户: {}", username);
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("查询所有用户");
        return userMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        logger.info("创建用户: {}", user.getUsername());
        
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(UserErrorCode.USERNAME_EXISTS);
        }
        
        // 密码加密
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_CREATE_FAILED);
        }
        
        logger.info("用户创建成功: {}", user.getUsername());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        logger.info("更新用户: {}", user.getId());
        
        // 检查用户是否存在
        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        
        int result = userMapper.update(user);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_UPDATE_FAILED);
        }
        
        logger.info("用户更新成功: {}", user.getId());
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        logger.info("删除用户: {}", id);
        
        // 检查用户是否存在
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        
        int result = userMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_DELETE_FAILED);
        }
        
        logger.info("用户删除成功: {}", id);
    }

    @Override
    public User login(String username, String password) {
        logger.info("用户登录: {}", username);
        
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        
        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new BusinessException(UserErrorCode.PASSWORD_ERROR);
        }
        
        logger.info("用户登录成功: {}", username);
        return user;
    }
}