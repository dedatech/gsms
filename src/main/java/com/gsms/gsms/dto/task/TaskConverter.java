package com.gsms.gsms.dto.task;

import com.gsms.gsms.domain.entity.Task;

/**
 * 任务对象转换器
 */
public class TaskConverter {
    
    /**
     * 创建请求转任务实体
     */
    public static Task toTask(TaskCreateReq req) {
        if (req == null) {
            return null;
        }
        Task task = new Task();
        task.setProjectId(req.getProjectId());
        task.setIterationId(req.getIterationId());
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setType(req.getType() != null ? req.getType().getCode() : null);
        task.setPriority(req.getPriority() != null ? req.getPriority().getCode() : null);
        task.setAssigneeId(req.getAssigneeId());
        task.setStatus(req.getStatus() != null ? req.getStatus().getCode() : null);
        task.setPlanStartDate(req.getPlanStartDate());
        task.setPlanEndDate(req.getPlanEndDate());
        task.setEstimateHours(req.getEstimateHours());
        return task;
    }
    
    /**
     * 更新请求转任务实体
     */
    public static Task toTask(TaskUpdateReq req) {
        if (req == null) {
            return null;
        }
        Task task = new Task();
        task.setId(req.getId());
        task.setProjectId(req.getProjectId());
        task.setIterationId(req.getIterationId());
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setType(req.getType() != null ? req.getType().getCode() : null);
        task.setPriority(req.getPriority() != null ? req.getPriority().getCode() : null);
        task.setAssigneeId(req.getAssigneeId());
        task.setStatus(req.getStatus() != null ? req.getStatus().getCode() : null);
        task.setPlanStartDate(req.getPlanStartDate());
        task.setPlanEndDate(req.getPlanEndDate());
        task.setActualStartDate(req.getActualStartDate());
        task.setActualEndDate(req.getActualEndDate());
        task.setEstimateHours(req.getEstimateHours());
        return task;
    }
}
