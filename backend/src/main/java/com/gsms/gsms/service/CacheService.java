package com.gsms.gsms.service;

import com.gsms.gsms.dto.user.UserInfoResp;

import java.util.List;
import java.util.Optional;

/**
 * 实体缓存服务接口
 * 提供 User 和 Department 的内存缓存功能
 *
 * 设计原则：
 * 1. 系统启动时加载所有数据到内存（缓存预热）
 * 2. 增删改操作时同步更新缓存
 * 3. 使用 ConcurrentHashMap 保证线程安全
 * 4. 提供直接提取数据的方法，避免暴露整个 Map
 */
public interface CacheService {

    // ========== User 缓存操作 ==========

    /**
     * 根据 ID 获取用户昵称（从缓存）
     * @param userId 用户ID
     * @return 昵称，不存在返回 null
     */
    String getUserNicknameById(Long userId);

    /**
     * 根据 ID 获取部门名称（从缓存）
     * @param departmentId 部门ID
     * @return 部门名称，不存在返回 null
     */
    String getDepartmentNameById(Long departmentId);

    /**
     * 批量填充用户响应的关联信息（从缓存）
     * 自动填充 departmentName, createUserName, updateUserName
     * @param respList 用户响应列表
     */
    void enrichUserInfoRespList(List<UserInfoResp> respList);

    /**
     * 填充单个用户响应的关联信息（从缓存）
     * @param resp 用户响应
     */
    void enrichUserInfoResp(UserInfoResp resp);

    /**
     * 根据 ID 获取用户实体（从缓存，供内部使用）
     */
    Optional<com.gsms.gsms.model.entity.User> getUserById(Long id);

    /**
     * 根据用户名获取用户实体（从缓存，供内部使用）
     */
    Optional<com.gsms.gsms.model.entity.User> getUserByUsername(String username);

    /**
     * 添加或更新用户缓存
     */
    void putUser(com.gsms.gsms.model.entity.User user);

    /**
     * 移除用户缓存
     */
    void removeUser(Long id);

    /**
     * 刷新用户缓存（重新从数据库加载）
     */
    void refreshUserCache();

    // ========== Department 缓存操作 ==========

    /**
     * 根据 ID 获取部门实体（从缓存，供内部使用）
     */
    Optional<com.gsms.gsms.model.entity.Department> getDepartmentById(Long id);

    /**
     * 添加或更新部门缓存
     */
    void putDepartment(com.gsms.gsms.model.entity.Department department);

    /**
     * 移除部门缓存
     */
    void removeDepartment(Long id);

    /**
     * 刷新部门缓存（重新从数据库加载）
     */
    void refreshDepartmentCache();

    // ========== 全局操作 ==========

    /**
     * 刷新所有缓存（重新从数据库加载）
     */
    void refreshAll();
}
