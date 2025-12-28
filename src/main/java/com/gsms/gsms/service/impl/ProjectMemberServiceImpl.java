package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.ProjectMember;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 项目成员服务实现
 */
@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final AuthService authService;

    public ProjectMemberServiceImpl(ProjectMemberMapper projectMemberMapper, ProjectMapper projectMapper, UserMapper userMapper, AuthService authService) {
        this.projectMemberMapper = projectMemberMapper;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.authService = authService;
    }

    @Override
    public List<ProjectMember> listMembersByProjectId(Long projectId) {
        Long currentUserId = requireLogin();
        // 系统级角色可查看所有项目成员
        if (!authService.canViewAllProjects(currentUserId)) {
            // 其他用户必须是项目成员之一
            List<Long> userIds = projectMemberMapper.selectUserIdsByProjectId(projectId);
            if (userIds == null || userIds.isEmpty() || !userIds.contains(currentUserId)) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
        return projectMemberMapper.selectMembersByProjectId(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(Long projectId, List<Long> userIds, Integer roleType) {
        Long currentUserId = requireLogin();
        // 校验项目存在
        if (projectMapper.selectById(projectId) == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
        // 只有系统级角色或项目成员可以维护成员列表
        if (!authService.canViewAllProjects(currentUserId)) {
            List<Long> memberIds = projectMemberMapper.selectUserIdsByProjectId(projectId);
            if (memberIds == null || !memberIds.contains(currentUserId)) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        // 去重并排除已存在成员
        Set<Long> targetUserIds = new HashSet<>(userIds);
        List<Long> existUserIds = projectMemberMapper.selectUserIdsByProjectId(projectId);
        if (existUserIds != null && !existUserIds.isEmpty()) {
            targetUserIds.removeAll(existUserIds);
        }
        for (Long userId : targetUserIds) {
            if (userMapper.selectById(userId) == null) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND);
            }
            projectMemberMapper.insertProjectMember(projectId, userId, roleType, currentUserId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRole(Long projectId, Long userId, Integer roleType) {
        Long currentUserId = requireLogin();
        if (projectMapper.selectById(projectId) == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
        if (!authService.canViewAllProjects(currentUserId)) {
            List<Long> memberIds = projectMemberMapper.selectUserIdsByProjectId(projectId);
            if (memberIds == null || !memberIds.contains(currentUserId)) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
        int updated = projectMemberMapper.updateProjectMemberRole(projectId, userId, roleType);
        if (updated <= 0) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long projectId, Long userId) {
        Long currentUserId = requireLogin();
        if (projectMapper.selectById(projectId) == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
        if (!authService.canViewAllProjects(currentUserId)) {
            List<Long> memberIds = projectMemberMapper.selectUserIdsByProjectId(projectId);
            if (memberIds == null || !memberIds.contains(currentUserId)) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
        int updated = projectMemberMapper.deleteProjectMember(projectId, userId);
        if (updated <= 0) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
    }

    private Long requireLogin() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        return currentUserId;
    }
}
