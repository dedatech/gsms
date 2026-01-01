package com.gsms.gsms.service;

import java.util.Date;
import java.util.Map;

/**
 * 数据统计服务接口
 * 提供项目、用户、部门、任务等维度的工时数据统计功能
 */
public interface StatisticsService {

    /**
     * 获取项目工时统计数据
     *
     * 统计指定项目在时间范围内的工时数据，包括总工时、用户分布等信息
     *
     * @param projectId 项目ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 包含totalHours、userHoursDistribution等统计数据的Map
     */
    Map<String, Object> getProjectWorkHourStatistics(Long projectId, Date startDate, Date endDate);

    /**
     * 获取用户工时统计数据
     *
     * 统计指定用户在时间范围内的工时数据，包括总工时、项目分布等信息
     *
     * @param userId 用户ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 包含totalHours、projectHoursDistribution等统计数据的Map
     */
    Map<String, Object> getUserWorkHourStatistics(Long userId, Date startDate, Date endDate);

    /**
     * 获取部门工时统计数据
     *
     * 统计指定部门及所有下属用户在时间范围内的工时数据
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 包含totalHours、userHoursDistribution等统计数据的Map
     */
    Map<String, Object> getDepartmentWorkHourStatistics(Long departmentId, Date startDate, Date endDate);

    /**
     * 获取任务工时统计数据
     *
     * 统计指定任务的实际工时、预估工时及偏差情况
     *
     * @param taskId 任务ID
     * @return 包含totalHours、estimateHours、variance等统计数据的Map
     */
    Map<String, Object> getTaskWorkHourStatistics(Long taskId);

    /**
     * 获取项目任务完成度统计数据
     *
     * 统计指定项目中各状态任务的数量及完成率
     *
     * @param projectId 项目ID
     * @return 包含totalTasks、doneTasks、completionRate等统计数据的Map
     */
    Map<String, Object> getProjectTaskCompletionStatistics(Long projectId);

    /**
     * 获取工时趋势统计数据
     *
     * 按日期统计工时投入趋势，支持按项目或用户维度过滤
     *
     * @param projectId 项目ID（可选，为null时统计所有项目）
     * @param userId 用户ID（可选，为null时统计所有用户）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含trendData、totalHours等统计数据的Map
     */
    Map<String, Object> getWorkHourTrendStatistics(Long projectId, Long userId, Date startDate, Date endDate);
}
