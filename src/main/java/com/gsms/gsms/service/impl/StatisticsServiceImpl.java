package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.entity.WorkHour;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.repository.WorkHourMapper;
import com.gsms.gsms.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private WorkHourMapper workHourMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> getProjectWorkHourStatistics(Long projectId, Date startDate, Date endDate) {
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
        
        return result;
    }

    @Override
    public Map<String, Object> getUserWorkHourStatistics(Long userId, Date startDate, Date endDate) {
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
        
        return result;
    }

    @Override
    public Map<String, Object> getDepartmentWorkHourStatistics(Long departmentId, Date startDate, Date endDate) {
        Map<String, Object> result = new HashMap<>();
        
        List<User> departmentUsers = userMapper.selectByDepartmentId(departmentId);
        List<Long> userIds = departmentUsers.stream().map(User::getId).collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            result.put("departmentId", departmentId);
            result.put("totalHours", BigDecimal.ZERO);
            result.put("totalRecords", 0);
            result.put("userCount", 0);
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
        
        return result;
    }

    @Override
    public Map<String, Object> getTaskWorkHourStatistics(Long taskId) {
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
        
        return result;
    }

    @Override
    public Map<String, Object> getProjectTaskCompletionStatistics(Long projectId) {
        Map<String, Object> result = new HashMap<>();
        
        List<Task> tasks = taskMapper.selectByProjectId(projectId);
        
        long totalTasks = tasks.size();
        long todoTasks = tasks.stream().filter(t -> t.getStatus().getCode() == 1).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus().getCode() == 2).count();
        long doneTasks = tasks.stream().filter(t -> t.getStatus().getCode() == 3).count();
        
        double completionRate = totalTasks > 0 ? (double) doneTasks / totalTasks * 100 : 0;
        
        result.put("projectId", projectId);
        result.put("totalTasks", totalTasks);
        result.put("todoTasks", todoTasks);
        result.put("inProgressTasks", inProgressTasks);
        result.put("doneTasks", doneTasks);
        result.put("completionRate", String.format("%.2f%%", completionRate));
        
        return result;
    }

    @Override
    public Map<String, Object> getWorkHourTrendStatistics(Long projectId, Long userId, Date startDate, Date endDate) {
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
        
        return result;
    }
}
