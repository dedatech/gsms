package com.gsms.gsms.controller;

import com.gsms.gsms.dto.task.TaskConverter;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskQueryReq;
import com.gsms.gsms.dto.task.TaskUpdateReq;
import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.TaskService;
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
    public Result<Task> getTaskById(@PathVariable Long id) {
        logger.info("根据ID查询任务: {}", id);
        Task task = taskService.getTaskById(id);
        logger.info("成功查询到任务: {}", task.getTitle());
        return Result.success(task);
    }

    /**
     * 根据项目ID查询任务
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "根据项目ID查询任务")
    public Result<List<Task>> getTasksByProjectId(@PathVariable Long projectId) {
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
    public Result<List<Task>> getTasksByCondition(TaskQueryReq req) {
        logger.info("根据条件查询任务: projectId={}, assigneeId={}, status={}", 
                    req.getProjectId(), req.getAssigneeId(), req.getStatus());
        Integer statusCode = req.getStatus() != null ? req.getStatus().getCode() : null;
        List<Task> tasks = taskService.getTasksByCondition(req.getProjectId(), req.getAssigneeId(), statusCode);
        logger.info("根据条件查询到{}个任务", tasks.size());
        return Result.success(tasks);
    }

    /**
     * 创建任务
     */
    @PostMapping
    @Operation(summary = "创建任务")
    public Result<Task> createTask(@RequestBody @Valid TaskCreateReq req) {
        logger.info("创建任务: {}", req.getTitle());
        Task task = TaskConverter.toTask(req);
        Task createdTask = taskService.createTask(task);
        logger.info("任务创建成功: {}", createdTask.getTitle());
        return Result.success(createdTask);
    }

    /**
     * 更新任务
     */
    @PutMapping
    @Operation(summary = "更新任务")
    public Result<Task> updateTask(@RequestBody @Valid TaskUpdateReq req) {
        logger.info("更新任务: {}", req.getId());
        Task task = TaskConverter.toTask(req);
        Task updatedTask = taskService.updateTask(task);
        logger.info("任务更新成功: {}", updatedTask.getId());
        return Result.success(updatedTask);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    public Result<String> deleteTask(@PathVariable Long id) {
        logger.info("删除任务: {}", id);
        taskService.deleteTask(id);
        logger.info("任务删除成功: {}", id);
        return Result.success("任务删除成功");
    }
}