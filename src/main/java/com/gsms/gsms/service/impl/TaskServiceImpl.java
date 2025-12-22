package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.domain.enums.errorcode.TaskErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public Task getTaskById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    @Override
    public List<Task> getTasksByProjectId(Long projectId) {
        return taskMapper.selectByProjectId(projectId);
    }

    @Override
    public List<Task> getTasksByCondition(Long projectId, Long assigneeId, Integer status) {
        return taskMapper.selectByCondition(projectId, assigneeId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task createTask(Task task) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        task.setCreateUserId(currentUserId);
        
        int result = taskMapper.insert(task);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_CREATE_FAILED);
        }
        
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task updateTask(Task task) {
        // 检查任务是否存在
        Task existTask = taskMapper.selectById(task.getId());
        if (existTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }
        
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        task.setUpdateUserId(currentUserId);
        
        int result = taskMapper.update(task);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
        }
        
        return taskMapper.selectById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        // 检查任务是否存在
        Task existTask = taskMapper.selectById(id);
        if (existTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }
        
        int result = taskMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_DELETE_FAILED);
        }
    }
}