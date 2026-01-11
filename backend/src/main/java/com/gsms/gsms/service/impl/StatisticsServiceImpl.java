package com.gsms.gsms.service.impl;

import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.entity.WorkHour;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.repository.WorkHourMapper;
import com.gsms.gsms.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    private final ProjectMapper projectMapper;

    public StatisticsServiceImpl(WorkHourMapper workHourMapper, TaskMapper taskMapper, UserMapper userMapper, ProjectMapper projectMapper) {
        this.workHourMapper = workHourMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    public Map<String, Object> getProjectWorkHourStatistics(Long projectId, LocalDate startDate, LocalDate endDate) {
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
    public Map<String, Object> getUserWorkHourStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
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
    public Map<String, Object> getDepartmentWorkHourStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
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
    public Map<String, Object> getWorkHourTrendStatistics(Long projectId, Long userId, LocalDate startDate, LocalDate endDate) {
        logger.debug("统计工时趋势: projectId={}, userId={}, startDate={}, endDate={}", projectId, userId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();

        List<WorkHour> workHours = workHourMapper.selectByCondition(userId, projectId, null, startDate, endDate);

        Map<LocalDate, BigDecimal> dailyHoursMap = workHours.stream()
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

    @Override
    public Map<String, Object> getDashboardData() {
        logger.debug("获取首页看板数据");

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }

        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 获取用户可访问的项目列表
        List<Project> allProjects = projectMapper.selectAccessibleProjects(userId);
        result.put("projectCount", allProjects.size());

        // 获取项目列表（限制5个）
        List<Map<String, Object>> projectList = allProjects.stream()
                .limit(5)
                .map(project -> {
                    Map<String, Object> projectData = new HashMap<>();
                    projectData.put("id", project.getId());
                    projectData.put("name", project.getName());
                    projectData.put("code", project.getCode());
                    projectData.put("status", project.getStatus());
                    return projectData;
                })
                .collect(Collectors.toList());
        result.put("projects", projectList);

        // 获取用户的待办任务（TODO和IN_PROGRESS状态）
        List<Task> allTodoTasks = new ArrayList<>();
        List<Task> allTasks = taskMapper.selectAccessibleTasksByCondition(userId, null, userId, null);
        for (Task task : allTasks) {
            if (task.getStatus() == TaskStatus.TODO || task.getStatus() == TaskStatus.IN_PROGRESS) {
                allTodoTasks.add(task);
            }
        }

        result.put("pendingTaskCount", allTodoTasks.size());

        // 获取待办任务列表（限制5个，按ID降序）
        List<Map<String, Object>> taskList = allTodoTasks.stream()
                .sorted((t1, t2) -> t2.getId().compareTo(t1.getId()))
                .limit(5)
                .map(task -> {
                    Map<String, Object> taskData = new HashMap<>();
                    taskData.put("id", task.getId());
                    taskData.put("title", task.getTitle());
                    taskData.put("status", task.getStatus());
                    taskData.put("priority", task.getPriority());
                    taskData.put("projectId", task.getProjectId());
                    taskData.put("planEndDate", task.getPlanEndDate());
                    return taskData;
                })
                .collect(Collectors.toList());
        result.put("pendingTasks", taskList);

        // 计算今日工时
        List<WorkHour> todayWorkHours = workHourMapper.selectByCondition(userId, null, null, today, today);
        BigDecimal todayHours = todayWorkHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todayHours", todayHours);

        // 计算本周工时
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        List<WorkHour> weekWorkHours = workHourMapper.selectByCondition(userId, null, null, weekStart, weekEnd);
        BigDecimal weekHours = weekWorkHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("weekHours", weekHours);

        // 计算本月工时
        LocalDate monthStart = LocalDate.of(today.getYear(), today.getMonth(), 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        List<WorkHour> monthWorkHours = workHourMapper.selectByCondition(userId, null, null, monthStart, monthEnd);
        BigDecimal monthHours = monthWorkHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("monthHours", monthHours);

        // 总工时
        List<WorkHour> allWorkHours = workHourMapper.selectByCondition(userId, null, null, null, null);
        BigDecimal totalHours = allWorkHours.stream()
                .map(WorkHour::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalHours", totalHours);

        logger.info("首页看板数据获取完成: userId={}, projectCount={}, pendingTaskCount={}, todayHours={}",
                userId, allProjects.size(), allTodoTasks.size(), todayHours);
        return result;
    }
}
