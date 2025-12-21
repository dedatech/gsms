package com.gsms.gsms.service;

import com.gsms.gsms.entity.Task;

import java.util.List;

/**
 * 任务服务接口
 */
public interface TaskService {
    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务实体
     */
    Task getTaskById(Long id);

    /**
     * 根据项目ID查询任务
     * @param projectId 项目ID
     * @return 任务列表
     */
    List<Task> getTasksByProjectId(Long projectId);

    /**
     * 根据条件查询任务
     * @param projectId 项目ID
     * @param assigneeId 负责人ID
     * @param status 任务状态
     * @return 任务列表
     */
    List<Task> getTasksByCondition(Long projectId, Long assigneeId, Integer status);

    /**
     * 创建任务
     * @param task 任务实体
     * @return 是否成功
     */
    boolean createTask(Task task);

    /**
     * 更新任务
     * @param task 任务实体
     * @return 是否成功
     */
    boolean updateTask(Task task);

    /**
     * 删除任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean deleteTask(Long id);
}