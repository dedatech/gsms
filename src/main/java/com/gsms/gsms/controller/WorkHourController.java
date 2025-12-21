package com.gsms.gsms.controller;

import com.gsms.gsms.common.Result;
import com.gsms.gsms.entity.WorkHour;
import com.gsms.gsms.service.WorkHourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 工时记录控制器
 */
@RestController
@RequestMapping("/api/work-hours")
@Tag(name = "工时管理接口", description = "工时记录相关的API接口")
public class WorkHourController {
    private static final Logger logger = LoggerFactory.getLogger(WorkHourController.class);

    @Autowired
    private WorkHourService workHourService;

    /**
     * 根据ID查询工时记录
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询工时记录")
    public Result<WorkHour> getWorkHourById(
            @Parameter(description = "工时记录ID")
            @PathVariable Long id) {
        logger.info("根据ID查询工时记录: {}", id);
        WorkHour workHour = workHourService.getWorkHourById(id);
        if (workHour != null) {
            logger.info("成功查询到工时记录: ID={}", workHour.getId());
            return Result.success(workHour);
        } else {
            logger.warn("未找到ID为{}的工时记录", id);
            return Result.error("工时记录不存在");
        }
    }

    /**
     * 根据用户ID查询工时记录
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询工时记录")
    public Result<List<WorkHour>> getWorkHoursByUserId(
            @Parameter(description = "用户ID")
            @PathVariable Long userId) {
        logger.info("根据用户ID查询工时记录: {}", userId);
        List<WorkHour> workHours = workHourService.getWorkHoursByUserId(userId);
        logger.info("根据用户ID查询到{}条工时记录", workHours.size());
        return Result.success(workHours);
    }

    /**
     * 根据项目ID查询工时记录
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "根据项目ID查询工时记录")
    public Result<List<WorkHour>> getWorkHoursByProjectId(
            @Parameter(description = "项目ID")
            @PathVariable Long projectId) {
        logger.info("根据项目ID查询工时记录: {}", projectId);
        List<WorkHour> workHours = workHourService.getWorkHoursByProjectId(projectId);
        logger.info("根据项目ID查询到{}条工时记录", workHours.size());
        return Result.success(workHours);
    }

    /**
     * 根据条件查询工时记录
     */
    @GetMapping("/search")
    @Operation(summary = "根据条件查询工时记录")
    public Result<List<WorkHour>> getWorkHoursByCondition(
            @Parameter(description = "用户ID")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "项目ID")
            @RequestParam(required = false) Long projectId,
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) String endDate) {
        
        logger.info("根据条件查询工时记录: userId={}, projectId={}, startDate={}, endDate={}", userId, projectId, startDate, endDate);
        Date start = null;
        Date end = null;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            if (startDate != null && !startDate.isEmpty()) {
                start = sdf.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = sdf.parse(endDate);
            }
        } catch (ParseException e) {
            logger.error("日期格式错误: {}", e.getMessage());
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
        
        List<WorkHour> workHours = workHourService.getWorkHoursByCondition(userId, projectId, start, end);
        logger.info("根据条件查询到{}条工时记录", workHours.size());
        return Result.success(workHours);
    }

    /**
     * 创建工时记录
     */
    @PostMapping
    @Operation(summary = "创建工时记录")
    public Result<String> createWorkHour(
            @Parameter(description = "工时记录信息")
            @RequestBody WorkHour workHour) {
        logger.info("创建工时记录: userId={}, projectId={}, taskId={}", workHour.getUserId(), workHour.getProjectId(), workHour.getTaskId());
        boolean success = workHourService.createWorkHour(workHour);
        if (success) {
            logger.info("工时记录创建成功: ID={}", workHour.getId());
            return Result.success("工时记录创建成功");
        } else {
            logger.error("工时记录创建失败: userId={}, projectId={}, taskId={}", workHour.getUserId(), workHour.getProjectId(), workHour.getTaskId());
            return Result.error("工时记录创建失败");
        }
    }

    /**
     * 更新工时记录
     */
    @PutMapping
    @Operation(summary = "更新工时记录")
    public Result<String> updateWorkHour(
            @Parameter(description = "工时记录信息")
            @RequestBody WorkHour workHour) {
        logger.info("更新工时记录: {}", workHour.getId());
        boolean success = workHourService.updateWorkHour(workHour);
        if (success) {
            logger.info("工时记录更新成功: {}", workHour.getId());
            return Result.success("工时记录更新成功");
        } else {
            logger.error("工时记录更新失败: {}", workHour.getId());
            return Result.error("工时记录更新失败");
        }
    }

    /**
     * 删除工时记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除工时记录")
    public Result<String> deleteWorkHour(
            @Parameter(description = "工时记录ID")
            @PathVariable Long id) {
        logger.info("删除工时记录: {}", id);
        boolean success = workHourService.deleteWorkHour(id);
        if (success) {
            logger.info("工时记录删除成功: {}", id);
            return Result.success("工时记录删除成功");
        } else {
            logger.error("工时记录删除失败: {}", id);
            return Result.error("工时记录删除失败");
        }
    }
}