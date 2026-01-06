package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.entity.Department;
import com.gsms.gsms.model.enums.UserStatus;
import com.gsms.gsms.model.enums.errorcode.UserErrorCode;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.user.UserQueryReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserUpdateReq;
import com.gsms.gsms.dto.user.UserConverter;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.PasswordUtil;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.repository.DepartmentMapper;
import com.gsms.gsms.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * Service层接收DTO，内部转换为Entity
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    public UserServiceImpl(UserMapper userMapper, DepartmentMapper departmentMapper) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
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
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
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
        user.setStatus(UserStatus.NORMAL); // 默认状态为正常

        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(UserErrorCode.USERNAME_EXISTS);
        }

        // 密码加密
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        user.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        user.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(UserErrorCode.USER_CREATE_FAILED);
        }

        logger.info("用户创建成功: {}", user.getUsername());
        UserInfoResp resp = UserInfoResp.from(user);
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

        logger.info("用户更新成功: {}", user.getId());
        // 重新查询获取更新后的数据
        User updatedUser = userMapper.selectById(user.getId());
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

    /**
     * 填充用户响应的关联信息（部门名称、创建人姓名、更新人姓名）
     */
    private void enrichUserInfoResp(UserInfoResp resp) {
        if (resp == null) {
            return;
        }

        // 查询部门名称
        if (resp.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(resp.getDepartmentId());
            if (department != null) {
                resp.setDepartmentName(department.getName());
            }
        }

        // 查询创建人姓名
        if (resp.getCreateUserId() != null) {
            User creator = userMapper.selectById(resp.getCreateUserId());
            if (creator != null) {
                resp.setCreateUserName(creator.getNickname());
            }
        }

        // 查询更新人姓名
        if (resp.getUpdateUserId() != null) {
            User updater = userMapper.selectById(resp.getUpdateUserId());
            if (updater != null) {
                resp.setUpdateUserName(updater.getNickname());
            }
        }
    }

    /**
     * 批量填充用户响应的关联信息（优化性能，减少数据库查询次数）
     */
    private void enrichUserInfoRespList(List<UserInfoResp> respList) {
        if (respList == null || respList.isEmpty()) {
            return;
        }

        // 收集所有需要查询的ID
        List<Long> departmentIds = respList.stream()
                .map(UserInfoResp::getDepartmentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Long> userIds = respList.stream()
                .flatMap(resp -> java.util.stream.Stream.of(resp.getCreateUserId(), resp.getUpdateUserId()))
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 使用批量查询优化性能
        final Map<Long, String> departmentMap;
        if (departmentIds.isEmpty()) {
            departmentMap = java.util.Collections.emptyMap();
        } else {
            List<Department> departments = departmentMapper.selectByIds(departmentIds);
            departmentMap = departments.stream()
                    .collect(Collectors.toMap(
                            Department::getId,
                            Department::getName,
                            (existing, replacement) -> existing
                    ));
        }

        final Map<Long, String> userMap;
        if (userIds.isEmpty()) {
            userMap = java.util.Collections.emptyMap();
        } else {
            List<User> users = userMapper.selectByIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            User::getNickname,
                            (existing, replacement) -> existing
                    ));
        }

        // 填充数据
        respList.forEach(resp -> {
            if (resp.getDepartmentId() != null) {
                resp.setDepartmentName(departmentMap.get(resp.getDepartmentId()));
            }
            if (resp.getCreateUserId() != null) {
                resp.setCreateUserName(userMap.get(resp.getCreateUserId()));
            }
            if (resp.getUpdateUserId() != null) {
                resp.setUpdateUserName(userMap.get(resp.getUpdateUserId()));
            }
        });
    }
}
