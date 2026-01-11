package com.gsms.gsms.controller;

import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

/**
 * 数据统计控制器
 * 提供工时、任务等多维度数据统计API
 */
@Tag(name = "数据统计", description = "工时数据统计相关接口")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取项目工时统计数据
     *
     * 统计指定项目在时间范围内的工时投入情况
     *
     * @param projectId 项目ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 工时统计数据，包含总工时、用户分布等
     */
    @Operation(summary = "获取项目工时统计")
    @GetMapping("/project/{projectId}")
    public Result<Map<String, Object>> getProjectWorkHourStatistics(
            @Parameter(description = "项目ID", required = true) @PathVariable Long projectId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        logger.info("查询项目工时统计: projectId={}, startDate={}, endDate={}", projectId, startDate, endDate);
        Map<String, Object> statistics = statisticsService.getProjectWorkHourStatistics(projectId, startDate, endDate);
        logger.info("项目工时统计查询成功: projectId={}, totalHours={}", projectId, statistics.get("totalHours"));
        return Result.success(statistics);
    }

    /**
     * 获取用户工时统计数据
     *
     * 统计指定用户在时间范围内的工时数据
     *
     * @param userId 用户ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 工时统计数据，包含总工时、项目分布等
     */
    @Operation(summary = "获取用户工时统计")
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserWorkHourStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        logger.info("查询用户工时统计: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        Map<String, Object> statistics = statisticsService.getUserWorkHourStatistics(userId, startDate, endDate);
        logger.info("用户工时统计查询成功: userId={}, totalHours={}", userId, statistics.get("totalHours"));
        return Result.success(statistics);
    }

    /**
     * 获取部门工时统计数据
     *
     * 统计指定部门及所有下属用户在时间范围内的工时数据
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 工时统计数据，包含总工时、用户分布等
     */
    @Operation(summary = "获取部门工时统计")
    @GetMapping("/department/{departmentId}")
    public Result<Map<String, Object>> getDepartmentWorkHourStatistics(
            @Parameter(description = "部门ID", required = true) @PathVariable Long departmentId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        logger.info("查询部门工时统计: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);
        Map<String, Object> statistics = statisticsService.getDepartmentWorkHourStatistics(departmentId, startDate, endDate);
        logger.info("部门工时统计查询成功: departmentId={}, totalHours={}", departmentId, statistics.get("totalHours"));
        return Result.success(statistics);
    }

    /**
     * 获取任务工时统计数据
     *
     * 统计指定任务的实际工时、预估工时及偏差情况
     *
     * @param taskId 任务ID
     * @return 工时统计数据，包含总工时、预估工时、偏差等
     */
    @Operation(summary = "获取任务工时统计")
    @GetMapping("/task/{taskId}")
    public Result<Map<String, Object>> getTaskWorkHourStatistics(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        logger.info("查询任务工时统计: taskId={}", taskId);
        Map<String, Object> statistics = statisticsService.getTaskWorkHourStatistics(taskId);
        logger.info("任务工时统计查询成功: taskId={}, totalHours={}", taskId, statistics.get("totalHours"));
        return Result.success(statistics);
    }

    /**
     * 获取项目任务完成度统计数据
     *
     * 统计指定项目中各状态任务的数量及完成率
     *
     * @param projectId 项目ID
     * @return 任务完成度统计数据，包含总任务数、已完成数、完成率等
     */
    @Operation(summary = "获取项目任务完成度统计")
    @GetMapping("/project/{projectId}/completion")
    public Result<Map<String, Object>> getProjectTaskCompletionStatistics(
            @Parameter(description = "项目ID", required = true) @PathVariable Long projectId) {
        logger.info("查询项目任务完成度统计: projectId={}", projectId);
        Map<String, Object> statistics = statisticsService.getProjectTaskCompletionStatistics(projectId);
        logger.info("项目任务完成度统计查询成功: projectId={}, completionRate={}", projectId, statistics.get("completionRate"));
        return Result.success(statistics);
    }

    /**
     * 获取工时趋势统计数据
     *
     * 按日期统计工时投入趋势，支持按项目或用户维度过滤
     *
     * @param projectId 项目ID（可选）
     * @param userId 用户ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势统计数据，包含时间序列数据、总工时等
     */
    @Operation(summary = "获取工时趋势统计")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getWorkHourTrendStatistics(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        logger.info("查询工时趋势统计: projectId={}, userId={}, startDate={}, endDate={}", projectId, userId, startDate, endDate);
        Map<String, Object> statistics = statisticsService.getWorkHourTrendStatistics(projectId, userId, startDate, endDate);
        logger.info("工时趋势统计查询成功: totalHours={}", statistics.get("totalHours"));
        return Result.success(statistics);
    }

    /**
     * 获取首页看板数据
     *
     * 聚合当前用户的首页看板所需的所有统计数据
     *
     * @return 首页看板数据，包含项目数、待办任务数、今日工时、本周工时、待办任务列表、项目列表等
     */
    @Operation(summary = "获取首页看板数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        logger.info("查询首页看板数据");
        Map<String, Object> dashboardData = statisticsService.getDashboardData();
        logger.info("首页看板数据查询成功: {}", dashboardData.keySet());
        return Result.success(dashboardData);
    }
}
