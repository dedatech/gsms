package com.gsms.gsms.controller;

import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@Tag(name = "数据统计", description = "工时数据统计相关接口")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "获取项目工时统计")
    @GetMapping("/project/{projectId}")
    public Result<Map<String, Object>> getProjectWorkHourStatistics(
            @Parameter(description = "项目ID", required = true) @PathVariable Long projectId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> statistics = statisticsService.getProjectWorkHourStatistics(projectId, startDate, endDate);
        return Result.success(statistics);
    }

    @Operation(summary = "获取用户工时统计")
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserWorkHourStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> statistics = statisticsService.getUserWorkHourStatistics(userId, startDate, endDate);
        return Result.success(statistics);
    }

    @Operation(summary = "获取部门工时统计")
    @GetMapping("/department/{departmentId}")
    public Result<Map<String, Object>> getDepartmentWorkHourStatistics(
            @Parameter(description = "部门ID", required = true) @PathVariable Long departmentId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> statistics = statisticsService.getDepartmentWorkHourStatistics(departmentId, startDate, endDate);
        return Result.success(statistics);
    }

    @Operation(summary = "获取任务工时统计")
    @GetMapping("/task/{taskId}")
    public Result<Map<String, Object>> getTaskWorkHourStatistics(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        Map<String, Object> statistics = statisticsService.getTaskWorkHourStatistics(taskId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取项目任务完成度统计")
    @GetMapping("/project/{projectId}/completion")
    public Result<Map<String, Object>> getProjectTaskCompletionStatistics(
            @Parameter(description = "项目ID", required = true) @PathVariable Long projectId) {
        Map<String, Object> statistics = statisticsService.getProjectTaskCompletionStatistics(projectId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取工时趋势统计")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getWorkHourTrendStatistics(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> statistics = statisticsService.getWorkHourTrendStatistics(projectId, userId, startDate, endDate);
        return Result.success(statistics);
    }
}
