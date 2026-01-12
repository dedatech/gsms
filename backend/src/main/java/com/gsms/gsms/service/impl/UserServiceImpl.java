package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.UserStatus;
import com.gsms.gsms.model.enums.errorcode.UserErrorCode;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.user.UserQueryReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserUpdateReq;
import com.gsms.gsms.dto.user.PasswordChangeReq;
import com.gsms.gsms.dto.user.UserConverter;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.PasswordUtil;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.repository.DepartmentMapper;
import com.gsms.gsms.repository.RoleMapper;
import com.gsms.gsms.repository.PermissionMapper;
import com.gsms.gsms.service.UserService;
import com.gsms.gsms.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 * Service层接收DTO，内部转换为Entity
 * 使用CacheService优化查询性能
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final CacheService cacheService;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public UserServiceImpl(UserMapper userMapper, DepartmentMapper departmentMapper, CacheService cacheService,
                             RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.cacheService = cacheService;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public UserInfoResp getById(Long id) {
        logger.debug("根据ID查询用户: {}", id);
        User user = getUserById(id);
        UserInfoResp resp = UserInfoResp.from(user);
        enrichUserInfoResp(resp);
        return resp;
    }

    @Override
    public UserInfoResp getByUsername(String username) {
        logger.debug("根据用户名查询用户: {}", username);
        // 从缓存获取用户
        User user = cacheService.getUserByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        UserInfoResp resp = UserInfoResp.from(user);
        enrichUserInfoResp(resp);
        return resp;
    }

    @Override
    public PageResult<UserInfoResp> findAll(UserQueryReq userQueryReq) {
        logger.debug("根据条件分页查询用户: userQueryReq={}", userQueryReq);
        User user =  UserConverter.toUser(userQueryReq);
        // 使用PageHelper进行分页
        PageHelper.startPage(userQueryReq.getPageNum(), userQueryReq.getPageSize());
        List<User> users = userMapper.findAll(user);

        PageInfo<User> pageInfo = new PageInfo<>(users);
        List<UserInfoResp> userInfoRespList = UserInfoResp.from(users);

        // 批量填充关联信息
        enrichUserInfoRespList(userInfoRespList);

        return PageResult.success(userInfoRespList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResp create(UserCreateReq createReq) {
        logger.info("创建用户: {}", createReq.getUsername());

        // DTO转Entity
        User user = UserConverter.toUser(createReq);

        // 设置默认状态为禁用（需要管理员审核）
        user.setStatus(UserStatus.DISABLED);

        // 密码加密
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        user.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        user.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(UserErrorCode.USERNAME_EXISTS);
        }

        // 密码加密
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));

        // 设置审计字段

        user.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        user.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_CREATE_FAILED);
        }

        // 重新查询获取完整数据（包含 createTime、updateTime 等数据库默认值）
        User createdUser = userMapper.selectById(user.getId());
        cacheService.putUser(createdUser);

        logger.info("用户创建成功: {}", user.getUsername());
        UserInfoResp resp = UserInfoResp.from(createdUser);
        enrichUserInfoResp(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResp update(UserUpdateReq updateReq) {
        logger.info("更新用户: {}", updateReq.getId());

        // 检查用户是否存在
        getUserById(updateReq.getId());

        // DTO转Entity
        User user = UserConverter.toUser(updateReq);

        // 如果密码为空，不更新密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            User existingUser = getUserById(updateReq.getId());
            user.setPassword(existingUser.getPassword());
        } else {
            // 新密码需要加密
            user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        }

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        user.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = userMapper.update(user);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_UPDATE_FAILED);
        }

        // 更新缓存：重新查询获取完整数据
        User updatedUser = userMapper.selectById(user.getId());
        cacheService.putUser(updatedUser);

        logger.info("用户更新成功: {}", user.getId());
        UserInfoResp resp = UserInfoResp.from(updatedUser);
        enrichUserInfoResp(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        logger.info("删除用户: {}", id);

        // 检查用户是否存在
        getUserById(id);

        int result = userMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_DELETE_FAILED);
        }

        // 从缓存中移除
        cacheService.removeUser(id);

        logger.info("用户删除成功: {}", id);
    }

    @Override
    public User login(String username, String password) {
        logger.info("用户登录: {}", username);

        // 从缓存获取用户
        User user = cacheService.getUserByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new BusinessException(UserErrorCode.PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() != UserStatus.NORMAL) {
            logger.warn("用户登录失败 - 用户已禁用: username={}", username);
            throw new BusinessException(UserErrorCode.USER_DISABLED);
        }

        logger.info("用户登录成功: {}", username);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(PasswordChangeReq req) {
        Long currentUserId = UserContext.getCurrentUserId();
        logger.info("用户修改密码: userId={}", currentUserId);

        // 获取当前用户
        User user = getUserById(currentUserId);

        // 验证旧密码
        if (!PasswordUtil.verify(req.getOldPassword(), user.getPassword())) {
            logger.warn("用户修改密码失败 - 旧密码错误: userId={}", currentUserId);
            throw new BusinessException(UserErrorCode.PASSWORD_ERROR);
        }

        // 加密新密码
        String encryptedNewPassword = PasswordUtil.encrypt(req.getNewPassword());

        // 更新密码
        user.setPassword(encryptedNewPassword);
        user.setPasswordResetRequired(0); // 清除密码重置标志
        user.setUpdateUserId(currentUserId);

        int result = userMapper.update(user);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_UPDATE_FAILED);
        }

        // 更新缓存
        User updatedUser = userMapper.selectById(currentUserId);
        cacheService.putUser(updatedUser);

        logger.info("用户密码修改成功: userId={}", currentUserId);
    }

    /**
     * 根据ID查询用户实体（内部方法）
     */
    private User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    // ========== 内部方法：数据填充 ==========

    /**
     * 填充单个 UserInfoResp 的创建人、更新人、部门信息
     */
    private void enrichUserInfoResp(UserInfoResp resp) {
        // 填充创建人信息
        if (resp.getCreateUserId() != null) {
            String creatorName = cacheService.getUserNicknameById(resp.getCreateUserId());
            resp.setCreateUserName(creatorName);
        }
        // 填充更新人信息
        if (resp.getUpdateUserId() != null) {
            String updaterName = cacheService.getUserNicknameById(resp.getUpdateUserId());
            resp.setUpdateUserName(updaterName);
        }
        // 填充部门信息
        if (resp.getDepartmentId() != null) {
            String departmentName = cacheService.getDepartmentNameById(resp.getDepartmentId());
            resp.setDepartmentName(departmentName);
        }
    }

    /**
     * 批量填充 UserInfoResp 列表的创建人、更新人、部门信息
     */
    private void enrichUserInfoRespList(List<UserInfoResp> respList) {
        if (respList == null || respList.isEmpty()) {
            return;
        }
        for (UserInfoResp resp : respList) {
            enrichUserInfoResp(resp);
        }
    }

    // ========== 角色和权限管理方法 ==========

    @Override
    public List<Long> getRoleIds(Long userId) {
        logger.debug("查询用户角色列表: userId={}", userId);
        // 检查用户是否存在
        getUserById(userId);
        return roleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        logger.info("为用户分配角色: userId={}, roleIds={}", userId, roleIds);
        // 检查用户是否存在
        getUserById(userId);

        // 删除用户现有的所有角色
        roleMapper.deleteUserRoles(userId);

        // 分配新的角色
        if (roleIds != null && !roleIds.isEmpty()) {
            roleMapper.insertUserRoles(userId, roleIds);
        }

        logger.info("用户角色分配成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long userId, Long roleId) {
        logger.info("移除用户角色: userId={}, roleId={}", userId, roleId);
        // 检查用户是否存在
        getUserById(userId);

        int result = roleMapper.deleteUserRole(userId, roleId);
        if (result <= 0) {
            logger.warn("移除用户角色失败: userId={}, roleId={}", userId, roleId);
        }

        logger.info("用户角色移除成功: userId={}, roleId={}", userId, roleId);
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        logger.debug("查询用户权限编码列表: userId={}", userId);
        // 检查用户是否存在
        getUserById(userId);
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }
}
