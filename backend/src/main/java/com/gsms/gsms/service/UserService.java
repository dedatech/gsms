package com.gsms.gsms.service;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.user.UserQueryReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserUpdateReq;
import com.gsms.gsms.dto.user.PasswordChangeReq;
import com.gsms.gsms.infra.common.PageResult;

/**
 * 用户服务接口
 * Service层接收DTO，内部转换为Entity
 */
public interface UserService {
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户响应DTO
     */
    UserInfoResp getById(Long id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户响应DTO
     */
    UserInfoResp getByUsername(String username);

    /**
     * 根据条件分页查询用户
     * @param userQueryReq 查询条件
     * @return 分页结果
     */
    PageResult<UserInfoResp> findAll(UserQueryReq userQueryReq);

    /**
     * 创建用户
     * @param createReq 创建请求DTO
     * @return 创建成功的用户响应DTO
     */
    UserInfoResp create(UserCreateReq createReq);

    /**
     * 更新用户
     * @param updateReq 更新请求DTO
     * @return 更新后的用户响应DTO
     */
    UserInfoResp update(UserUpdateReq updateReq);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void delete(Long id);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户实体
     */
    User login(String username, String password);

    /**
     * 修改密码
     * @param req 修改密码请求DTO
     */
    void changePassword(PasswordChangeReq req);

    /**
     * 查询用户的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    java.util.List<Long> getRoleIds(Long userId);

    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, java.util.List<Long> roleIds);

    /**
     * 移除用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void removeRole(Long userId, Long roleId);

    /**
     * 查询用户的权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表
     */
    java.util.List<String> getPermissionCodes(Long userId);
}