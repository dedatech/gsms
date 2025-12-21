package com.gsms.gsms.controller;

import com.gsms.gsms.common.Result;
import com.gsms.gsms.entity.Task;
import com.gsms.gsms.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理接口", description = "任务相关的API接口")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    /**
     * 根据ID查询任务
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询任务")
    public Result<Task> getTaskById(
            @Parameter(description = "任务ID")
            @PathVariable Long id) {
        logger.info("根据ID查询任务: {}", id);
        Task task = taskService.getTaskById(id);
        if (task != null) {
            logger.info("成功查询到任务: {}", task.getTitle());
            return Result.success(task);
        } else {
            logger.warn("未找到ID为{}的任务", id);
            return Result.error("任务不存在");
        }
    }

    /**
     * 根据项目ID查询任务
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "根据项目ID查询任务")
    public Result<List<Task>> getTasksByProjectId(
            @Parameter(description = "项目ID")
            @PathVariable Long projectId) {
        logger.info("根据项目ID查询任务: {}", projectId);
        List<Task> tasks = taskService.getTasksByProjectId(projectId);
        logger.info("根据项目ID查询到{}个任务", tasks.size());
        return Result.success(tasks);
    }

    /**
     * 根据条件查询任务
     */
    @GetMapping("/search")
    @Operation(summary = "根据条件查询任务")
    public Result<List<Task>> getTasksByCondition(
            @Parameter(description = "项目ID")
            @RequestParam(required = false) Long projectId,
            @Parameter(description = "负责人ID")
            @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "任务状态")
            @RequestParam(required = false) Integer status) {
        logger.info("根据条件查询任务: projectId={}, assigneeId={}, status={}", projectId, assigneeId, status);
        List<Task> tasks = taskService.getTasksByCondition(projectId, assigneeId, status);
        logger.info("根据条件查询到{}个任务", tasks.size());
        return Result.success(tasks);
    }

    /**
     * 创建任务
     */
    @PostMapping
    @Operation(summary = "创建任务")
    public Result<String> createTask(
            @Parameter(description = "任务信息")
            @RequestBody Task task) {
        logger.info("创建任务: {}", task.getTitle());
        boolean success = taskService.createTask(task);
        if (success) {
            logger.info("任务创建成功: {}", task.getTitle());
            return Result.success("任务创建成功");
        } else {
            logger.error("任务创建失败: {}", task.getTitle());
            return Result.error("任务创建失败");
        }
    }

    /**
     * 更新任务
     */
    @PutMapping
    @Operation(summary = "更新任务")
    public Result<String> updateTask(
            @Parameter(description = "任务信息")
            @RequestBody Task task) {
        logger.info("更新任务: {}", task.getId());
        boolean success = taskService.updateTask(task);
        if (success) {
            logger.info("任务更新成功: {}", task.getId());
            return Result.success("任务更新成功");
        } else {
            logger.error("任务更新失败: {}", task.getId());
            return Result.error("任务更新失败");
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    public Result<String> deleteTask(
            @Parameter(description = "任务ID")
            @PathVariable Long id) {
        logger.info("删除任务: {}", id);
        boolean success = taskService.deleteTask(id);
        if (success) {
            logger.info("任务删除成功: {}", id);
            return Result.success("任务删除成功");
        } else {
            logger.error("任务删除失败: {}", id);
            return Result.error("任务删除失败");
        }
    }
}