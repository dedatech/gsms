package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.dto.task.TaskPageQuery;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.dto.task.TaskInfoResp;

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
     * 根据条件分页查询任务
     * @return 分页结果
     */
    PageResult<TaskInfoResp> findAll(TaskPageQuery taskPageQuery);

    /**
     * 创建任务
     * @param task 任务实体
     * @return 创建成功的任务实体
     * @throws BusinessException 任务信息无效时抛出异常
     */
    Task createTask(Task task);

    /**
     * 更新任务
     * @param task 任务实体
     * @return 更新后的任务实体
     * @throws BusinessException 任务不存在时抛出异常
     */
    Task updateTask(Task task);

    /**
     * 删除任务
     * @param id 任务ID
     * @throws BusinessException 任务不存在时抛出异常
     */
    void deleteTask(Long id);
}