package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.dto.permission.PermissionCreateReq;
import com.gsms.gsms.dto.permission.PermissionConverter;
import com.gsms.gsms.dto.permission.PermissionInfoResp;
import com.gsms.gsms.dto.permission.PermissionQueryReq;
import com.gsms.gsms.dto.permission.PermissionUpdateReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.model.entity.Permission;
import com.gsms.gsms.model.enums.errorcode.PermissionErrorCode;
import com.gsms.gsms.repository.PermissionMapper;
import com.gsms.gsms.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限服务实现类
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public PermissionInfoResp getById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_NOT_FOUND);
        }
        PermissionInfoResp resp = PermissionInfoResp.from(permission);
        // 加载角色ID列表
        resp.setRoleIds(permissionMapper.selectRoleIdsByPermissionId(id));
        return resp;
    }

    @Override
    public PageResult<PermissionInfoResp> findAll(PermissionQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Permission> list = permissionMapper.selectByCondition(req.getName(), req.getCode());
        PageInfo<Permission> pageInfo = new PageInfo<>(list);
        List<PermissionInfoResp> respList = PermissionInfoResp.from(list);
        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    public List<PermissionInfoResp> getAll() {
        List<Permission> list = permissionMapper.selectAll();
        return PermissionInfoResp.from(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionInfoResp create(PermissionCreateReq createReq) {
        // 检查权限编码是否已存在
        Permission existingPermission = permissionMapper.selectByCode(createReq.getCode());
        if (existingPermission != null) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_CODE_EXISTS);
        }

        // DTO转Entity
        Permission permission = PermissionConverter.toEntity(createReq);

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        permission.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        permission.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = permissionMapper.insert(permission);
        if (result <= 0) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_CREATE_FAILED);
        }
        return PermissionInfoResp.from(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionInfoResp update(PermissionUpdateReq updateReq) {
        // 检查权限是否存在
        Permission existingPermission = permissionMapper.selectById(updateReq.getId());
        if (existingPermission == null) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_NOT_FOUND);
        }

        // 如果修改了编码，检查新编码是否已被其他权限使用
        if (updateReq.getCode() != null && !updateReq.getCode().equals(existingPermission.getCode())) {
            Permission codePermission = permissionMapper.selectByCode(updateReq.getCode());
            if (codePermission != null && !codePermission.getId().equals(updateReq.getId())) {
                throw new BusinessException(PermissionErrorCode.PERMISSION_CODE_EXISTS);
            }
        }

        // DTO转Entity
        Permission permission = PermissionConverter.toEntity(updateReq);

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        permission.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = permissionMapper.update(permission);
        if (result <= 0) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_UPDATE_FAILED);
        }

        Permission updatedPermission = permissionMapper.selectById(permission.getId());
        return PermissionInfoResp.from(updatedPermission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_NOT_FOUND);
        }

        // 检查权限是否正在使用
        List<Long> roleIds = permissionMapper.selectRoleIdsByPermissionId(id);
        if (!roleIds.isEmpty()) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_IN_USE);
        }

        int result = permissionMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(PermissionErrorCode.PERMISSION_DELETE_FAILED);
        }
    }
}
