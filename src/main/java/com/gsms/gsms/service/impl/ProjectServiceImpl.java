package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.domain.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.dto.project.ProjectQueryReq;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.dto.project.ProjectConverter;
import com.gsms.gsms.infra.common.PageResult;
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
    public Project getById(Long id) {
        // 先鉴权再查询
        checkProjectAccess(id);

        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    @Override
    public PageResult<Project> findAll(ProjectQueryReq req) {
        Long currentUserId = UserContext.getCurrentUserId();
        Integer statusCode = req.getStatus() != null ? req.getStatus().getCode() : null;

        PageHelper.startPage(req.getPageNum(), req.getPageSize());

        // 系统管理员和业务相关角色可以查看所有项目
        List<Project> projects;
        if (authService.canViewAllProjects(currentUserId)) {
            projects = projectMapper.selectByCondition(req.getName(), statusCode);
        } else {
            // 普通用户只查询自己参与的项目（在SQL层过滤）
            projects = projectMapper.selectAccessibleProjectsByCondition(currentUserId, req.getName(), statusCode);
        }

        PageInfo<Project> pageInfo = new PageInfo<>(projects);
        return PageResult.success(projects, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project create(ProjectCreateReq createReq) {
        // DTO转Entity
        Project project = ProjectConverter.toProject(createReq);

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
    public Project update(ProjectUpdateReq updateReq) {
        // 先鉴权
        checkProjectAccess(updateReq.getId());

        // 检查项目是否存在
        Project existProject = projectMapper.selectById(updateReq.getId());
        if (existProject == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }

        // DTO转Entity
        Project project = ProjectConverter.toProject(updateReq);

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
    public void delete(Long id) {
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