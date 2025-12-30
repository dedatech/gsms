package com.gsms.gsms.service.impl;

import com.gsms.gsms.model.entity.ProjectMember;
import com.gsms.gsms.model.enums.ProjectMemberRole;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.ProjectMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ProjectMemberServiceImpl.class);

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
        logger.info("查询项目成员列表: projectId={}", projectId);
        Long currentUserId = requireLogin();
        // 系统级角色可查看所有项目成员
        if (!authService.canViewAllProjects(currentUserId)) {
            // 其他用户必须是项目成员之一
            List<Long> userIds = projectMemberMapper.selectUserIdsByProjectId(projectId);
            if (userIds == null || userIds.isEmpty() || !userIds.contains(currentUserId)) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
        List<ProjectMember> members = projectMemberMapper.selectMembersByProjectId(projectId);
        logger.info("查询到{}个项目成员", members.size());
        return members;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(Long projectId, List<Long> userIds, Integer roleType) {
        // 转换并验证角色类型
        ProjectMemberRole role = ProjectMemberRole.fromCode(roleType);
        logger.info("为项目添加成员: projectId={}, userIds={}, roleType={}", projectId, userIds, role.getDesc());

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
        int addedCount = 0;
        for (Long userId : targetUserIds) {
            if (userMapper.selectById(userId) == null) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND);
            }
            projectMemberMapper.insertProjectMember(projectId, userId, roleType, currentUserId);
            addedCount++;
        }
        logger.info("成功为项目添加{}个成员: projectId={}, role={}", addedCount, projectId, role.getDesc());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRole(Long projectId, Long userId, Integer roleType) {
        // 转换并验证角色类型
        ProjectMemberRole role = ProjectMemberRole.fromCode(roleType);
        logger.info("更新项目成员角色: projectId={}, userId={}, roleType={}", projectId, userId, role.getDesc());

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
        logger.info("项目成员角色更新成功: projectId={}, userId={}, newRole={}", projectId, userId, role.getDesc());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long projectId, Long userId) {
        logger.info("从项目移除成员: projectId={}, userId={}", projectId, userId);
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
        logger.info("项目成员移除成功: projectId={}, userId={}", projectId, userId);
    }

    private Long requireLogin() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        return currentUserId;
    }
}
