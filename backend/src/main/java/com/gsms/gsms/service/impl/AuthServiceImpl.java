package com.gsms.gsms.service.impl;

import com.gsms.gsms.repository.PermissionMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.repository.RoleMapper;
import com.gsms.gsms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限与数据范围服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String PERM_PROJECT_VIEW_ALL = "PROJECT_VIEW_ALL";
    private static final String PERM_TASK_VIEW_ALL = "TASK_VIEW_ALL";
    private static final String PERM_WORKHOUR_VIEW_ALL = "WORKHOUR_VIEW_ALL";

    private final PermissionMapper permissionMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final RoleMapper roleMapper;

    public AuthServiceImpl(PermissionMapper permissionMapper, ProjectMemberMapper projectMemberMapper, RoleMapper roleMapper) {
        this.permissionMapper = permissionMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null) {
            return false;
        }
        Set<String> codes = getPermissionCodes(userId);
        return codes.contains(permissionCode);
    }

    @Override
    public boolean canViewAllProjects(Long userId) {
        return hasPermission(userId, PERM_PROJECT_VIEW_ALL);
    }

    @Override
    public boolean canViewAllTasks(Long userId) {
        return hasPermission(userId, PERM_TASK_VIEW_ALL);
    }

    @Override
    public boolean canViewAllWorkHours(Long userId) {
        return hasPermission(userId, PERM_WORKHOUR_VIEW_ALL);
    }

    @Override
    public List<Long> getAccessibleProjectIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return projectMemberMapper.selectProjectIdsByUserId(userId);
    }

    private Set<String> getPermissionCodes(Long userId) {
        List<String> codes = permissionMapper.selectPermissionCodesByUserId(userId);
        if (codes == null || codes.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(codes);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || roleCode == null) {
            return false;
        }
        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(userId);
        return roleCodes != null && roleCodes.contains(roleCode);
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<String> codes = roleMapper.selectRoleCodesByUserId(userId);
        return codes != null ? codes : Collections.emptyList();
    }
}
