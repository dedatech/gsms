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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final AuthService authService;

    public ProjectServiceImpl(ProjectMapper projectMapper, ProjectMemberMapper projectMemberMapper, AuthService authService) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.authService = authService;
    }

    /**
     * 校验当前用户对项目的访问权限
     * @param projectId 项目ID
     * @throws BusinessException 无权限时抛出异常
     */
    private void checkProjectAccess(Long projectId) {
        Long currentUserId = UserContext.getCurrentUserId();
        
        // 系统管理员和业务相关角色可以访问所有项目
        if (authService.canViewAllProjects(currentUserId)) {
            return;
        }
        
        // 普通用户只能访问自己参与的项目
        List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
        if (projectIds == null || !projectIds.contains(projectId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }

    @Override
    public Project getProjectById(Long id) {
        // 先鉴权再查询
        checkProjectAccess(id);
        
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    @Override
    public List<Project> getAllProjects() {
        Long currentUserId = UserContext.getCurrentUserId();
        
        // 系统管理员和业务相关角色可以查看所有项目
        if (authService.canViewAllProjects(currentUserId)) {
            return projectMapper.selectAll();
        }
        
        // 普通用户只查询自己参与的项目（在SQL层过滤）
        return projectMapper.selectAccessibleProjects(currentUserId);
    }

    @Override
    public List<Project> getProjectsByCondition(String name, Integer status) {
        Long currentUserId = UserContext.getCurrentUserId();
            
        // 系统管理员和业务相关角色可以查看所有项目
        if (authService.canViewAllProjects(currentUserId)) {
            return projectMapper.selectByCondition(name, status);
        }
            
        // 普通用户只查询自己参与的项目（在SQL层过滤）
        return projectMapper.selectAccessibleProjectsByCondition(currentUserId, name, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createProject(Project project) {
        // 自动填充创建人（从登录态获取）
        Long currentUserId = UserContext.getCurrentUserId();
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
        // 先鉴权
        checkProjectAccess(project.getId());
        
        // 检查项目是否存在
        Project existProject = projectMapper.selectById(project.getId());
        if (existProject == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 自动填充更新人（从登录态获取）
        Long currentUserId = UserContext.getCurrentUserId();
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
        // 先鉴权
        checkProjectAccess(id);
        
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