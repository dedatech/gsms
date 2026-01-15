package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.model.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.model.enums.ProjectType;
import com.gsms.gsms.model.enums.ProjectStatus;
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
import com.gsms.gsms.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final AuthService authService;
    private final CacheService cacheService;

    public ProjectServiceImpl(ProjectMapper projectMapper, ProjectMemberMapper projectMemberMapper,
                             AuthService authService, CacheService cacheService) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.authService = authService;
        this.cacheService = cacheService;
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
        ProjectInfoResp resp = ProjectInfoResp.from(project);
        enrichProjectInfoResp(resp);
        return resp;
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
            query.setManagerId(currentUserId);
            projects = projectMapper.selectAccessibleProjectsByCondition(
                query
            );
        }

        log.info("projects.size(): {}", projects.size());

        PageInfo<Project> pageInfo = new PageInfo<>(projects);
        List<ProjectInfoResp> respList = ProjectInfoResp.from(projects);

        // 使用缓存填充创建人、更新人信息
        enrichProjectInfoRespList(respList);

        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectInfoResp create(ProjectCreateReq createReq) {
        // DTO转Entity
        Project project = ProjectConverter.toProject(createReq);

        // 如果项目编码为空，自动生成
        if (project.getCode() == null || project.getCode().trim().isEmpty()) {
            String generatedCode = generateProjectCode(project.getProjectType());
            project.setCode(generatedCode);
            log.info("自动生成项目编码: {}", generatedCode);
        } else {
            // 检查项目编码是否已存在, 存在则抛出异常
            Project existProject = getByCode(project.getCode());
            if (existProject != null) {
                throw new BusinessException(ProjectErrorCode.PROJECT_CODE_EXISTS);
            }
        }

        // 设置默认状态（如果前端没有传递）
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.NOT_STARTED);
            log.info("设置默认项目状态: NOT_STARTED");
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

        // 将项目创建人加入项目成员，默认为项目管理员角色 1
        // 注意：MyBatis会自动将生成的ID回填到project对象中，无需反查数据库
        projectMemberMapper.insertProjectMember(project.getId(), currentUserId, 1, currentUserId);

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

    // ========== 内部方法：数据填充 ==========

    /**
     * 填充单个 ProjectInfoResp 的创建人、更新人信息
     */
    private void enrichProjectInfoResp(ProjectInfoResp resp) {
        if (resp.getCreateUserId() != null) {
            String creatorName = cacheService.getUserNicknameById(resp.getCreateUserId());
            resp.setCreateUserName(creatorName);
        }
        if (resp.getUpdateUserId() != null) {
            String updaterName = cacheService.getUserNicknameById(resp.getUpdateUserId());
            resp.setUpdateUserName(updaterName);
        }
    }

    /**
     * 批量填充 ProjectInfoResp 列表的创建人、更新人信息
     */
    private void enrichProjectInfoRespList(List<ProjectInfoResp> respList) {
        if (respList == null || respList.isEmpty()) {
            return;
        }
        for (ProjectInfoResp resp : respList) {
            enrichProjectInfoResp(resp);
        }
    }

    /**
     * 自动生成项目编码
     * 规则：项目类型拼音前缀 + 递增数字
     * - 常规型项目（SCHEDULE）-> CG1, CG2, CG3...
     * - 中大型项目（LARGE_SCALE）-> DX1, DX2, DX3...
     *
     * @param projectType 项目类型
     * @return 生成的项目编码
     */
    private String generateProjectCode(ProjectType projectType) {
        // 确定前缀
        String prefix = (projectType == ProjectType.LARGE_SCALE) ? "DX" : "CG";

        // 查询该类型项目的最大编号
        Integer maxNumber = projectMapper.selectMaxNumberByPrefix(prefix);

        // 如果没有记录，从 1 开始；否则递增
        int nextNumber = (maxNumber == null) ? 1 : maxNumber + 1;

        String generatedCode = prefix + nextNumber;
        log.info("生成项目编码: 前缀={}, 最大编号={}, 新编码={}", prefix, maxNumber, generatedCode);

        return generatedCode;
    }
}