package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.model.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.dto.project.ProjectQueryReq;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.dto.project.ProjectConverter;
import com.gsms.gsms.dto.project.ProjectInfoResp;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final AuthService authService;

    public ProjectServiceImpl(ProjectMapper projectMapper, ProjectMemberMapper projectMemberMapper, AuthService authService) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.authService = authService;
    }

    @Override
    public ProjectInfoResp getById(Long id) {
        /**
         * 先判断项目是否存在，后鉴权
         */
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }
        // 鉴权再查询
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, id);
        return ProjectInfoResp.from(project);
    }

    /**
     * 根据项目编码查询项目项目信息，用于判断项目编码是否已存在
     * @param req 查询条件
     * @return
     */
    protected Project getByCode(String code) {
        Project project = projectMapper.selectByCode(code);
        return project;
    }
    @Override
    public PageResult<ProjectInfoResp> findAll(ProjectQueryReq req) {
        Long currentUserId = UserContext.getCurrentUserId();
        Project query = ProjectConverter.toProject(req);

        // 调试日志
        log.info("=== ProjectServiceImpl.findAll ===");
        log.info("req.getName(): {}", req.getName());
        log.info("req.getStatus(): {}", req.getStatus());
        log.info("query.getStatus(): {}", query.getStatus());
        if (query.getStatus() != null) {
            log.info("query.getStatus().getCode(): {}", query.getStatus().getCode());
        }

        // 系统管理员和业务相关角色可以查看所有项目
        List<Project> projects;
        boolean canViewAllProjects = authService.canViewAllProjects(currentUserId);
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        if (canViewAllProjects) {
            projects = projectMapper.selectByCondition(query);
        } else {
            // 普通用户只查询自己参与的项目（在SQL层过滤）
            projects = projectMapper.selectAccessibleProjectsByCondition(currentUserId, query);
        }

        log.info("projects.size(): {}", projects.size());

        PageInfo<Project> pageInfo = new PageInfo<>(projects);
        List<ProjectInfoResp> respList = ProjectInfoResp.from(projects);
        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectInfoResp create(ProjectCreateReq createReq) {
        // DTO转Entity
        Project project = ProjectConverter.toProject(createReq);
        // 检查项目编码是否已存在, 存在则抛出异常
        Project existProject = getByCode(project.getCode());
        if (existProject != null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_CODE_EXISTS);
        }
        // 自动填充创建人（从登录态获取）
        Long currentUserId = UserContext.getCurrentUserId();
        project.setCreateUserId(currentUserId);
        project.setUpdateUserId(currentUserId);

        // 创建时间和更新时间由数据库自动填充
        int result = projectMapper.insert(project);
        if (result <= 0) {
            throw new BusinessException(ProjectErrorCode.PROJECT_CREATE_FAILED);
        }
        // 反查数据库存入的项目信息,方便后续追加项目成员
        Project createdProject = getByCode(project.getCode());
        // 将项目创建人加入项目成员，默认为项目管理员角色 1
        projectMemberMapper.insertProjectMember(createdProject.getId(), currentUserId, 1, currentUserId);

        return ProjectInfoResp.from(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectInfoResp update(ProjectUpdateReq updateReq) {
        // 先鉴权
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, updateReq.getId());

        // 检查项目是否存在
        Project existProject = projectMapper.selectById(updateReq.getId());
        if (existProject == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }

        // DTO转Entity
        Project project = ProjectConverter.toProject(updateReq);

        // 自动填充更新人（从登录态获取）
        project.setUpdateUserId(currentUserId);

        // 更新时间由数据库自动填充
        int result = projectMapper.update(project);
        if (result <= 0) {
            throw new BusinessException(ProjectErrorCode.PROJECT_UPDATE_FAILED);
        }

        Project updatedProject = projectMapper.selectById(project.getId());
        return ProjectInfoResp.from(updatedProject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先鉴权
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, id);

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