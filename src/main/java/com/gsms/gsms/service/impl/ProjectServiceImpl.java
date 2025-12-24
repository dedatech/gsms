package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.domain.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    private AuthService authService;

    @Override
    public Project getProjectById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
    
        Long currentUserId = UserContext.getCurrentUserId();
        // 未登录直接视为未授权
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        // 系统管理员和业务相关角色可以查看所有项目
        if (!authService.canViewAllProjects(currentUserId)) {
            // 其他角色只能查看自己参与的项目
            List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
            if (projectIds == null || !projectIds.contains(project.getId())) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
        return project;
    }

    @Override
    public List<Project> getAllProjects() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    
        List<Project> projects = projectMapper.selectAll();
        // 系统管理员和业务相关角色可以查看所有项目
        if (authService.canViewAllProjects(currentUserId)) {
            return projects;
        }
    
        List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
        if (projectIds == null || projectIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Set<Long> idSet = new HashSet<>(projectIds);
        projects.removeIf(p -> p.getId() == null || !idSet.contains(p.getId()));
        return projects;
    }

    @Override
    public List<Project> getProjectsByCondition(String name, Integer status) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    
        List<Project> projects = projectMapper.selectByCondition(name, status);
        // 系统管理员和业务相关角色可以查看所有项目
        if (authService.canViewAllProjects(currentUserId)) {
            return projects;
        }
    
        List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
        if (projectIds == null || projectIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Set<Long> idSet = new HashSet<>(projectIds);
        projects.removeIf(p -> p.getId() == null || !idSet.contains(p.getId()));
        return projects;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createProject(Project project) {
        // 自动填充创建人（从登录态获取）
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        project.setCreateUserId(currentUserId);
    
        // 创建时间和更新时间由数据库自动填充
        int result = projectMapper.insert(project);
        if (result <= 0) {
            throw new BusinessException(ProjectErrorCode.PROJECT_CREATE_FAILED);
        }
    
        // 将项目创建人加入项目成员，默认为项目管理员角色 1
        projectMemberMapper.insertProjectMember(project.getId(), currentUserId, 1, currentUserId);
    
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(Project project) {
        // 检查项目是否存在
        Project existProject = projectMapper.selectById(project.getId());
        if (existProject == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 自动填充更新人（从登录态获取）
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        project.setUpdateUserId(currentUserId);
        
        // 更新时间由数据库自动填充
        int result = projectMapper.update(project);
        if (result <= 0) {
            throw new BusinessException(ProjectErrorCode.PROJECT_UPDATE_FAILED);
        }
        
        return projectMapper.selectById(project.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        // 检查项目是否存在
        Project existProject = projectMapper.selectById(id);
        if (existProject == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        
        int result = projectMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ProjectErrorCode.PROJECT_DELETE_FAILED);
        }
    }
}