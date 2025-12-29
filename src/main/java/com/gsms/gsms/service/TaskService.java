package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.dto.task.TaskQueryReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.dto.task.TaskInfoResp;

/**
 * 任务服务接口
 */
public interface TaskService {
    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务实体
     */
    Task getById(Long id);

    /**
     * 根据条件分页查询任务
     * @return 分页结果
     */
    PageResult<TaskInfoResp> findAll(TaskQueryReq taskQueryReq);

    /**
     * 创建任务
     * @param task 任务实体
     * @return 创建成功的任务实体
     */
    Task create(Task task);

    /**
     * 更新任务
     * @param task 任务实体
     * @return 更新后的任务实体
     */
    Task update(Task task);

    /**
     * 删除任务
     * @param id 任务ID
     */
    void delete(Long id);
}