package com.gsms.gsms.service.impl;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.entity.Department;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.repository.DepartmentMapper;
import com.gsms.gsms.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体缓存服务实现类
 * 使用 ConcurrentHashMap 实现线程安全的内存缓存
 */
@Service
public class CacheServiceImpl implements CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    // 用户缓存：ID -> User
    private final Map<Long, User> userCache = new ConcurrentHashMap<>();

    // 用户缓存：username -> User（用于按用户名查询）
    private final Map<String, User> userUsernameCache = new ConcurrentHashMap<>();

    // 部门缓存：ID -> Department
    private final Map<Long, Department> departmentCache = new ConcurrentHashMap<>();

    public CacheServiceImpl(UserMapper userMapper, DepartmentMapper departmentMapper) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
    }

    /**
     * 系统启动时初始化缓存（缓存预热）
     */
    @PostConstruct
    public void init() {
        logger.info("开始初始化缓存...");
        long startTime = System.currentTimeMillis();

        try {
            refreshUserCache();
            refreshDepartmentCache();

            long endTime = System.currentTimeMillis();
            logger.info("缓存初始化完成，耗时: {}ms, 用户数: {}, 部门数: {}",
                    endTime - startTime, userCache.size(), departmentCache.size());
        } catch (Exception e) {
            logger.error("缓存初始化失败", e);
            // 缓存初始化失败不影响系统启动
        }
    }

    // ========== User 缓存操作实现 ==========

    @Override
    public String getUserNicknameById(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userCache.get(userId);
        return user != null ? user.getNickname() : null;
    }

    @Override
    public String getDepartmentNameById(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        Department department = departmentCache.get(departmentId);
        return department != null ? department.getName() : null;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userCache.get(id));
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userUsernameCache.get(username));
    }

    @Override
    public void putUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        userCache.put(user.getId(), user);

        // 如果用户名不为空，也更新用户名索引
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            userUsernameCache.put(user.getUsername(), user);
        }

        logger.debug("更新用户缓存: id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public void removeUser(Long id) {
        if (id == null) {
            return;
        }

        User user = userCache.remove(id);
        if (user != null && user.getUsername() != null) {
            userUsernameCache.remove(user.getUsername());
        }

        logger.debug("移除用户缓存: id={}", id);
    }

    @Override
    public void refreshUserCache() {
        logger.info("开始刷新用户缓存...");
        long startTime = System.currentTimeMillis();

        try {
            // 从数据库加载所有用户
            List<User> users = userMapper.selectAll();

            // 清空旧缓存
            userCache.clear();
            userUsernameCache.clear();

            // 填充新缓存
            for (User user : users) {
                if (user.getId() != null) {
                    userCache.put(user.getId(), user);
                    if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                        userUsernameCache.put(user.getUsername(), user);
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            logger.info("用户缓存刷新完成，加载用户数: {}, 耗时: {}ms", userCache.size(), endTime - startTime);
        } catch (Exception e) {
            logger.error("用户缓存刷新失败", e);
            throw e;
        }
    }

    // ========== Department 缓存操作实现 ==========

    @Override
    public Optional<Department> getDepartmentById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(departmentCache.get(id));
    }

    @Override
    public void putDepartment(Department department) {
        if (department == null || department.getId() == null) {
            return;
        }

        departmentCache.put(department.getId(), department);
        logger.debug("更新部门缓存: id={}, name={}", department.getId(), department.getName());
    }

    @Override
    public void removeDepartment(Long id) {
        if (id == null) {
            return;
        }

        departmentCache.remove(id);
        logger.debug("移除部门缓存: id={}", id);
    }

    @Override
    public void refreshDepartmentCache() {
        logger.info("开始刷新部门缓存...");
        long startTime = System.currentTimeMillis();

        try {
            // 从数据库加载所有部门
            List<Department> departments = departmentMapper.selectAll();

            // 清空旧缓存
            departmentCache.clear();

            // 填充新缓存
            for (Department department : departments) {
                if (department.getId() != null) {
                    departmentCache.put(department.getId(), department);
                }
            }

            long endTime = System.currentTimeMillis();
            logger.info("部门缓存刷新完成，加载部门数: {}, 耗时: {}ms", departmentCache.size(), endTime - startTime);
        } catch (Exception e) {
            logger.error("部门缓存刷新失败", e);
            throw e;
        }
    }

    // ========== 全局操作实现 ==========

    @Override
    public void refreshAll() {
        logger.info("开始刷新所有缓存...");
        refreshUserCache();
        refreshDepartmentCache();
        logger.info("所有缓存刷新完成");
    }
}
