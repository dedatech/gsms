package com.gsms.gsms.controller;

import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskQueryReq;
import com.gsms.gsms.dto.task.TaskUpdateReq;
import com.gsms.gsms.dto.task.TaskStatusUpdateReq;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.dto.task.TaskInfoResp;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理接口", description = "任务相关的API接口")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 根据条件分页查询任务
     *
     * @param req 查询条件（项目ID、负责人ID、状态、分页参数）
     * @return 分页结果
     */
    @GetMapping("/search")
    @Operation(summary = "根据条件分页查询任务")
    public PageResult<TaskInfoResp> search(@Valid TaskQueryReq req) {
        logger.info("根据条件分页查询任务: projectId={}, assigneeId={}, status={}, pageNum={}, pageSize={}",
                    req.getProjectId(), req.getAssigneeId(), req.getStatus(), req.getPageNum(), req.getPageSize());
        return taskService.findAll(req);
    }

    /**
     * 根据ID查询任务
     *
     * @param id 任务ID
     * @return 任务详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询任务")
    public Result<TaskInfoResp> getById(@PathVariable Long id) {
        logger.info("根据ID查询任务: {}", id);
        Task task = taskService.getById(id);
        TaskInfoResp resp = TaskInfoResp.from(task);
        logger.info("成功查询到任务: {}", task.getTitle());
        return Result.success(resp);
    }

    /**
     * 创建任务
     *
     * @param req 任务创建请求对象
     * @return 创建的任务信息
     */
    @PostMapping
    @Operation(summary = "创建任务")
    public Result<TaskInfoResp> create(@RequestBody @Valid TaskCreateReq req) {
        logger.info("创建任务: {}", req.getTitle());
        Task createdTask = taskService.create(req);
        TaskInfoResp resp = TaskInfoResp.from(createdTask);
        logger.info("任务创建成功: {}", createdTask.getTitle());
        return Result.success(resp);
    }


    /**
     * 更新任务
     *
     * @param req 任务更新请求对象
     * @return 更新后的任务信息
     */
    @PutMapping
    @Operation(summary = "更新任务")
    public Result<TaskInfoResp> update(@RequestBody @Valid TaskUpdateReq req) {
        logger.info("更新任务: {}", req.getId());
        Task updatedTask = taskService.update(req);
        TaskInfoResp resp = TaskInfoResp.from(updatedTask);
        logger.info("任务更新成功: {}", updatedTask.getId());
        return Result.success(resp);
    }

    /**
     * 更新任务状态（轻量级接口，支持拖拽和快捷状态变更）
     *
     * @param req 任务状态更新请求对象
     * @return 更新后的任务信息
     */
    @PutMapping("/status")
    @Operation(summary = "更新任务状态")
    public Result<TaskInfoResp> updateStatus(@RequestBody @Valid TaskStatusUpdateReq req) {
        logger.info("更新任务状态: taskId={}, status={}", req.getId(), req.getStatus());
        Task updatedTask = taskService.updateStatus(req);
        TaskInfoResp resp = TaskInfoResp.from(updatedTask);
        logger.info("任务状态更新成功: {}", updatedTask.getId());
        return Result.success(resp);
    }

    /**
     * 删除任务
     *
     * @param id 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    public Result<String> delete(@PathVariable Long id) {
        logger.info("删除任务: {}", id);
        taskService.delete(id);
        logger.info("任务删除成功: {}", id);
        return Result.success("任务删除成功");
    }
}