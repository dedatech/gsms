package com.gsms.gsms.controller;

import com.gsms.gsms.dto.gantt.GanttDataResp;
import com.gsms.gsms.dto.gantt.TaskDateUpdateReq;
import com.gsms.gsms.dto.gantt.TaskLinkCreateReq;
import com.gsms.gsms.dto.gantt.TaskParentUpdateReq;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.GanttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

/**
 * 甘特图控制器
 */
@RestController
@RequestMapping("/api/gantt")
@Tag(name = "甘特图接口", description = "甘特图相关的API接口")
public class GanttController {
    private static final Logger logger = LoggerFactory.getLogger(GanttController.class);

    private final GanttService ganttService;

    public GanttController(GanttService ganttService) {
        this.ganttService = ganttService;
    }

    /**
     * 获取项目甘特图数据
     *
     * @param projectId 项目ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 甘特图数据（任务树形结构 + 依赖关系）
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "获取项目甘特图数据")
    public Result<GanttDataResp> getProjectGanttData(
            @Parameter(description = "项目ID", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate) {
        logger.info("获取项目甘特图数据: projectId={}, startDate={}, endDate={}", projectId, startDate, endDate);
        GanttDataResp data = ganttService.getProjectGanttData(projectId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 更新任务时间（拖拽后）
     *
     * @param taskId 任务ID
     * @param req 任务时间更新请求
     * @return 操作结果
     */
    @PutMapping("/task/{taskId}/dates")
    @Operation(summary = "更新任务时间")
    public Result<Void> updateTaskDates(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDateUpdateReq req) {
        logger.info("更新任务时间: taskId={}, planStartDate={}, planEndDate={}",
                taskId, req.getPlanStartDate(), req.getPlanEndDate());
        ganttService.updateTaskDates(taskId, req);
        return Result.success();
    }

    /**
     * 更新任务层级（拖拽改变父任务）
     *
     * @param taskId 任务ID
     * @param req 任务层级更新请求
     * @return 操作结果
     */
    @PutMapping("/task/{taskId}/parent")
    @Operation(summary = "更新任务层级")
    public Result<Void> updateTaskParent(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Valid @RequestBody TaskParentUpdateReq req) {
        logger.info("更新任务层级: taskId={}, parentId={}", taskId, req.getParentId());
        ganttService.updateTaskParent(taskId, req.getParentId());
        return Result.success();
    }

    /**
     * 创建任务依赖关系
     *
     * @param req 任务依赖关系创建请求
     * @return 操作结果
     */
    @PostMapping("/link")
    @Operation(summary = "创建任务依赖关系")
    public Result<Void> createTaskLink(@Valid @RequestBody TaskLinkCreateReq req) {
        logger.info("创建任务依赖关系: source={}, target={}, type={}",
                req.getSource(), req.getTarget(), req.getType());
        ganttService.createTaskLink(req);
        return Result.success();
    }

    /**
     * 删除任务依赖关系
     *
     * @param linkId 依赖关系ID
     * @return 操作结果
     */
    @DeleteMapping("/link/{linkId}")
    @Operation(summary = "删除任务依赖关系")
    public Result<Void> deleteTaskLink(
            @Parameter(description = "依赖关系ID", required = true)
            @PathVariable Long linkId) {
        logger.info("删除任务依赖关系: linkId={}", linkId);
        ganttService.deleteTaskLink(linkId);
        return Result.success();
    }
}
