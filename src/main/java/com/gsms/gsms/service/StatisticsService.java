package com.gsms.gsms.service;

import java.util.Date;
import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getProjectWorkHourStatistics(Long projectId, Date startDate, Date endDate);

    Map<String, Object> getUserWorkHourStatistics(Long userId, Date startDate, Date endDate);

    Map<String, Object> getDepartmentWorkHourStatistics(Long departmentId, Date startDate, Date endDate);

    Map<String, Object> getTaskWorkHourStatistics(Long taskId);

    Map<String, Object> getProjectTaskCompletionStatistics(Long projectId);

    Map<String, Object> getWorkHourTrendStatistics(Long projectId, Long userId, Date startDate, Date endDate);
}
