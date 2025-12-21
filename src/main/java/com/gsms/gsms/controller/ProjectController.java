package com.gsms.gsms.controller;

import com.gsms.gsms.common.Result;
import com.gsms.gsms.entity.Project;
import com.gsms.gsms.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 根据ID查询项目
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询项目")
    public Result<Project> getProjectById(
            @Parameter(description = "项目ID")
            @PathVariable Long id) {
        logger.info("根据ID查询项目: {}", id);
        Project project = projectService.getProjectById(id);
        if (project != null) {
            logger.info("成功查询到项目: {}", project.getName());
            return Result.success(project);
        } else {
            logger.warn("未找到ID为{}的项目", id);
            return Result.error("项目不存在");
        }
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
    public Result<List<Project>> getProjectsByCondition(
            @Parameter(description = "项目名称，模糊匹配")
            @RequestParam(required = false) String name,
            @Parameter(description = "项目状态")
            @RequestParam(required = false) Integer status) {
        logger.info("根据条件查询项目: name={}, status={}", name, status);
        List<Project> projects = projectService.getProjectsByCondition(name, status);
        logger.info("根据条件查询到{}个项目", projects.size());
        return Result.success(projects);
    }

    /**
     * 创建项目
     */
    @PostMapping
    @Operation(summary = "创建项目")
    public Result<String> createProject(
            @Parameter(description = "项目信息")
            @RequestBody Project project) {
        logger.info("创建项目: {}", project.getName());
        boolean success = projectService.createProject(project);
        if (success) {
            logger.info("项目创建成功: {}", project.getName());
            return Result.success("项目创建成功");
        } else {
            logger.error("项目创建失败: {}", project.getName());
            return Result.error("项目创建失败");
        }
    }

    /**
     * 更新项目
     */
    @PutMapping
    @Operation(summary = "更新项目")
    public Result<String> updateProject(
            @Parameter(description = "项目信息")
            @RequestBody Project project) {
        logger.info("更新项目: {}", project.getId());
        boolean success = projectService.updateProject(project);
        if (success) {
            logger.info("项目更新成功: {}", project.getId());
            return Result.success("项目更新成功");
        } else {
            logger.error("项目更新失败: {}", project.getId());
            return Result.error("项目更新失败");
        }
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目")
    public Result<String> deleteProject(
            @Parameter(description = "项目ID")
            @PathVariable Long id) {
        logger.info("删除项目: {}", id);
        boolean success = projectService.deleteProject(id);
        if (success) {
            logger.info("项目删除成功: {}", id);
            return Result.success("项目删除成功");
        } else {
            logger.error("项目删除失败: {}", id);
            return Result.error("项目删除失败");
        }
    }
}