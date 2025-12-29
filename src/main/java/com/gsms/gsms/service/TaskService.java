package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.dto.task.TaskQueryReq;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskUpdateReq;
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
     * @param createReq 创建请求DTO
     * @return 创建成功的任务实体
     */
    Task create(TaskCreateReq createReq);

    /**
     * 更新任务
     * @param updateReq 更新请求DTO
     * @return 更新后的任务实体
     */
    Task update(TaskUpdateReq updateReq);

    /**
     * 删除任务
     * @param id 任务ID
     */
    void delete(Long id);
}