package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.dto.role.RoleCreateReq;
import com.gsms.gsms.dto.role.RoleInfoResp;
import com.gsms.gsms.dto.role.RolePermissionAssignReq;
import com.gsms.gsms.dto.role.RoleQueryReq;
import com.gsms.gsms.dto.role.RoleUpdateReq;
import com.gsms.gsms.dto.role.RoleConverter;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.model.entity.Role;
import com.gsms.gsms.model.enums.errorcode.RoleErrorCode;
import com.gsms.gsms.repository.RoleMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    public RoleServiceImpl(RoleMapper roleMapper, UserMapper userMapper) {
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public RoleInfoResp getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(RoleErrorCode.ROLE_NOT_FOUND);
        }
        RoleInfoResp resp = RoleInfoResp.from(role);
        // 加载权限ID列表
        resp.setPermissionIds(roleMapper.selectPermissionIdsByRoleId(id));
        return resp;
    }

    @Override
    public PageResult<RoleInfoResp> findAll(RoleQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Role> list = roleMapper.selectByCondition(req.getName(), req.getCode(), req.getRoleLevel());
        PageInfo<Role> pageInfo = new PageInfo<>(list);
        List<RoleInfoResp> respList = RoleInfoResp.from(list);
        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleInfoResp create(RoleCreateReq createReq) {
        // 检查角色编码是否已存在
        Role existingRole = roleMapper.selectByCode(createReq.getCode());
        if (existingRole != null) {
            throw new BusinessException(RoleErrorCode.ROLE_CODE_EXISTS);
        }

        // DTO转Entity
        Role role = RoleConverter.toEntity(createReq);

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        role.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        role.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = roleMapper.insert(role);
        if (result <= 0) {
            throw new BusinessException(RoleErrorCode.ROLE_CREATE_FAILED);
        }
        return RoleInfoResp.from(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleInfoResp update(RoleUpdateReq updateReq) {
        // 检查角色是否存在
        Role existingRole = roleMapper.selectById(updateReq.getId());
        if (existingRole == null) {
            throw new BusinessException(RoleErrorCode.ROLE_NOT_FOUND);
        }

        // 如果修改了编码，检查新编码是否已被其他角色使用
        if (updateReq.getCode() != null && !updateReq.getCode().equals(existingRole.getCode())) {
            Role codeRole = roleMapper.selectByCode(updateReq.getCode());
            if (codeRole != null && !codeRole.getId().equals(updateReq.getId())) {
                throw new BusinessException(RoleErrorCode.ROLE_CODE_EXISTS);
            }
        }

        // DTO转Entity
        Role role = RoleConverter.toEntity(updateReq);

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        role.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = roleMapper.update(role);
        if (result <= 0) {
            throw new BusinessException(RoleErrorCode.ROLE_UPDATE_FAILED);
        }

        Role updatedRole = roleMapper.selectById(role.getId());
        return RoleInfoResp.from(updatedRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(RoleErrorCode.ROLE_NOT_FOUND);
        }

        // 检查角色是否正在使用
        List<Long> userIds = roleMapper.selectUserIdsByRoleId(id);
        if (!userIds.isEmpty()) {
            throw new BusinessException(RoleErrorCode.ROLE_IN_USE);
        }

        int result = roleMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(RoleErrorCode.ROLE_DELETE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(RolePermissionAssignReq req) {
        // 检查角色是否存在
        getById(req.getRoleId());

        // 删除角色现有的所有权限
        roleMapper.deletePermissionsByRoleId(req.getRoleId());

        // 分配新的权限
        if (!req.getPermissionIds().isEmpty()) {
            roleMapper.insertRolePermissions(req.getRoleId(), req.getPermissionIds());
        }
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermission(Long roleId, Long permissionId) {
        // 检查角色是否存在
        getById(roleId);

        int result = roleMapper.deleteRolePermission(roleId, permissionId);
        if (result <= 0) {
            throw new BusinessException(RoleErrorCode.ROLE_UPDATE_FAILED);
        }
    }

    @Override
    public List<Long> getUserIds(Long roleId) {
        return roleMapper.selectUserIdsByRoleId(roleId);
    }
}
