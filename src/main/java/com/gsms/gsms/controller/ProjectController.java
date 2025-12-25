package com.gsms.gsms.controller;

import com.gsms.gsms.dto.project.ProjectConverter;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectQueryReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.domain.entity.ProjectMember;
import com.gsms.gsms.domain.enums.ProjectStatus;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.ProjectMemberService;
import com.gsms.gsms.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 项目控制器
 */
@RestController
@RequestMapping("/api/projects")
@Tag(name = "项目管理接口", description = "项目相关的API接口")
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMemberService projectMemberService;

    /**
     * 根据ID查询项目
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询项目")
    public Result<Project> getProjectById(@PathVariable Long id) {
        logger.info("根据ID查询项目: {}", id);
        Project project = projectService.getProjectById(id);
        logger.info("成功查询到项目: {}", project.getName());
        return Result.success(project);
    }

    /**
     * 查询所有项目
     */
    @GetMapping
    @Operation(summary = "查询所有项目")
    public Result<List<Project>> getAllProjects() {
        logger.info("查询所有项目");
        List<Project> projects = projectService.getAllProjects();
        logger.info("成功查询到{}个项目", projects.size());
        return Result.success(projects);
    }

    /**
     * 根据条件查询项目
     */
    @GetMapping("/search")
    @Operation(summary = "根据条件查询项目")
    public Result<List<Project>> getProjectsByCondition(ProjectQueryReq req) {
        logger.info("根据条件查询项目: name={}, status={}", req.getName(), req.getStatus());
        Integer statusCode = req.getStatus() != null ? req.getStatus().getCode() : null;
        List<Project> projects = projectService.getProjectsByCondition(req.getName(), statusCode);
        logger.info("根据条件查询到{}个项目", projects.size());
        return Result.success(projects);
    }

    /**
     * 创建项目
     */
    @PostMapping
    @Operation(summary = "创建项目")
    public Result<Project> createProject(@Valid @RequestBody ProjectCreateReq req) {
        logger.info("创建项目: {}", req.getName());
        
        // 使用转换器将 DTO 转为 Entity
        Project project = ProjectConverter.toProject(req);
        
        Project createdProject = projectService.createProject(project);
        logger.info("项目创建成功: {}", createdProject.getName());
        return Result.success(createdProject);
    }

    /**
     * 更新项目
     */
    @PutMapping
    @Operation(summary = "更新项目")
    public Result<Project> updateProject(@Valid @RequestBody ProjectUpdateReq req) {
        logger.info("更新项目: {}", req.getId());
        
        // 使用转换器将 DTO 转为 Entity
        Project project = ProjectConverter.toProject(req);
        
        Project updatedProject = projectService.updateProject(project);
        logger.info("项目更新成功: {}", updatedProject.getId());
        return Result.success(updatedProject);
    }

    /**
     * 查询项目成员列表
     */
    @GetMapping("/{projectId}/members")
    @Operation(summary = "查询项目成员列表")
    public Result<List<ProjectMember>> listProjectMembers(@PathVariable Long projectId) {
        List<ProjectMember> members = projectMemberService.listMembersByProjectId(projectId);
        return Result.success(members);
    }

    /**
     * 为项目批量添加成员
     */
    @PostMapping("/{projectId}/members")
    @Operation(summary = "为项目批量添加成员")
    public Result<String> addProjectMembers(@PathVariable Long projectId,
                                            @RequestBody List<Long> userIds,
                                            @RequestParam(name = "roleType", defaultValue = "2") Integer roleType) {
        projectMemberService.addMembers(projectId, userIds, roleType);
        return Result.success("添加项目成员成功");
    }

    /**
     * 更新项目成员角色
     */
    @PutMapping("/{projectId}/members/{userId}")
    @Operation(summary = "更新项目成员角色")
    public Result<String> updateProjectMemberRole(@PathVariable Long projectId,
                                                  @PathVariable Long userId,
                                                  @RequestParam("roleType") Integer roleType) {
        projectMemberService.updateMemberRole(projectId, userId, roleType);
        return Result.success("更新项目成员角色成功");
    }

    /**
     * 从项目中移除成员
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    @Operation(summary = "从项目中移除成员")
    public Result<String> removeProjectMember(@PathVariable Long projectId,
                                              @PathVariable Long userId) {
        projectMemberService.removeMember(projectId, userId);
        return Result.success("移除项目成员成功");
    }
}