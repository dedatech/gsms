package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.entity.WorkHour;
import com.gsms.gsms.domain.enums.TaskStatus;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.repository.WorkHourMapper;
import com.gsms.gsms.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private final WorkHourMapper workHourMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public StatisticsServiceImpl(WorkHourMapper workHourMapper, TaskMapper taskMapper, UserMapper userMapper) {
        this.workHourMapper = workHourMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> getProjectWorkHourStatistics(Long projectId, Date startDate, Date endDate) {
        logger.debug("统计项目工时: projectId={}, startDate={}, endDate={}", projectId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();

        List<WorkHour> workHours = workHourMapper.selectByCondition(null, projectId, null, startDate, endDate);

        BigDecimal totalHours = workHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> userHoursMap = workHours.stream()
                .collect(Collectors.groupingBy(
                        WorkHour::getUserId,
                        Collectors.reducing(BigDecimal.ZERO, WorkHour::getHours, BigDecimal::add)
                ));

        result.put("projectId", projectId);
        result.put("totalHours", totalHours);
        result.put("totalRecords", workHours.size());
        result.put("userCount", userHoursMap.size());
        result.put("userHoursDistribution", userHoursMap);
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        logger.info("项目工时统计完成: projectId={}, totalHours={}, recordCount={}", projectId, totalHours, workHours.size());
        return result;
    }

    @Override
    public Map<String, Object> getUserWorkHourStatistics(Long userId, Date startDate, Date endDate) {
        logger.debug("统计用户工时: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();

        List<WorkHour> workHours = workHourMapper.selectByCondition(userId, null, null, startDate, endDate);

        BigDecimal totalHours = workHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> projectHoursMap = workHours.stream()
                .collect(Collectors.groupingBy(
                        WorkHour::getProjectId,
                        Collectors.reducing(BigDecimal.ZERO, WorkHour::getHours, BigDecimal::add)
                ));

        result.put("userId", userId);
        result.put("totalHours", totalHours);
        result.put("totalRecords", workHours.size());
        result.put("projectCount", projectHoursMap.size());
        result.put("projectHoursDistribution", projectHoursMap);
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        logger.info("用户工时统计完成: userId={}, totalHours={}, recordCount={}", userId, totalHours, workHours.size());
        return result;
    }

    @Override
    public Map<String, Object> getDepartmentWorkHourStatistics(Long departmentId, Date startDate, Date endDate) {
        logger.debug("统计部门工时: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();

        List<User> departmentUsers = userMapper.selectByDepartmentId(departmentId);
        List<Long> userIds = departmentUsers.stream().map(User::getId).collect(Collectors.toList());

        if (userIds.isEmpty()) {
            result.put("departmentId", departmentId);
            result.put("totalHours", BigDecimal.ZERO);
            result.put("totalRecords", 0);
            result.put("userCount", 0);
            logger.info("部门工时统计完成: departmentId={}, totalHours=0, userCount=0", departmentId);
            return result;
        }

        List<WorkHour> workHours = new ArrayList<>();
        for (Long userId : userIds) {
            workHours.addAll(workHourMapper.selectByCondition(userId, null, null, startDate, endDate));
        }

        BigDecimal totalHours = workHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> userHoursMap = workHours.stream()
                .collect(Collectors.groupingBy(
                        WorkHour::getUserId,
                        Collectors.reducing(BigDecimal.ZERO, WorkHour::getHours, BigDecimal::add)
                ));

        result.put("departmentId", departmentId);
        result.put("totalHours", totalHours);
        result.put("totalRecords", workHours.size());
        result.put("userCount", userHoursMap.size());
        result.put("userHoursDistribution", userHoursMap);
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        logger.info("部门工时统计完成: departmentId={}, totalHours={}, recordCount={}", departmentId, totalHours, workHours.size());
        return result;
    }

    @Override
    public Map<String, Object> getTaskWorkHourStatistics(Long taskId) {
        logger.debug("统计任务工时: taskId={}", taskId);

        Map<String, Object> result = new HashMap<>();

        List<WorkHour> workHours = workHourMapper.selectByTaskId(taskId);
        Task task = taskMapper.selectById(taskId);

        BigDecimal totalHours = workHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal estimateHours = task != null ? task.getEstimateHours() : BigDecimal.ZERO;
        BigDecimal variance = totalHours.subtract(estimateHours);

        result.put("taskId", taskId);
        result.put("totalHours", totalHours);
        result.put("estimateHours", estimateHours);
        result.put("variance", variance);
        result.put("totalRecords", workHours.size());

        logger.info("任务工时统计完成: taskId={}, totalHours={}, estimateHours={}", taskId, totalHours, estimateHours);
        return result;
    }

    @Override
    public Map<String, Object> getProjectTaskCompletionStatistics(Long projectId) {
        logger.debug("统计项目任务完成度: projectId={}", projectId);

        Map<String, Object> result = new HashMap<>();

        List<Task> tasks = taskMapper.selectByProjectId(projectId);

        long totalTasks = tasks.size();
        long todoTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long doneTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        double completionRate = totalTasks > 0 ? (double) doneTasks / totalTasks * 100 : 0;

        result.put("projectId", projectId);
        result.put("totalTasks", totalTasks);
        result.put("todoTasks", todoTasks);
        result.put("inProgressTasks", inProgressTasks);
        result.put("doneTasks", doneTasks);
        result.put("completionRate", String.format("%.2f%%", completionRate));

        logger.info("项目任务完成度统计完成: projectId={}, totalTasks={}, completionRate={}", projectId, totalTasks, result.get("completionRate"));
        return result;
    }

    @Override
    public Map<String, Object> getWorkHourTrendStatistics(Long projectId, Long userId, Date startDate, Date endDate) {
        logger.debug("统计工时趋势: projectId={}, userId={}, startDate={}, endDate={}", projectId, userId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();

        List<WorkHour> workHours = workHourMapper.selectByCondition(userId, projectId, null, startDate, endDate);

        Map<Date, BigDecimal> dailyHoursMap = workHours.stream()
                .collect(Collectors.groupingBy(
                        WorkHour::getWorkDate,
                        Collectors.reducing(BigDecimal.ZERO, WorkHour::getHours, BigDecimal::add)
                ));

        List<Map<String, Object>> trendData = dailyHoursMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("date", entry.getKey());
                    dataPoint.put("hours", entry.getValue());
                    return dataPoint;
                })
                .collect(Collectors.toList());

        BigDecimal totalHours = workHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("projectId", projectId);
        result.put("userId", userId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalHours", totalHours);
        result.put("trendData", trendData);

        logger.info("工时趋势统计完成: totalHours={}, dataPoints={}", totalHours, trendData.size());
        return result;
    }
}
