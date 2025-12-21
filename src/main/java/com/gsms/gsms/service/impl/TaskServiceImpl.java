package com.gsms.gsms.service.impl;

import com.gsms.gsms.entity.Task;
import com.gsms.gsms.mapper.TaskMapper;
import com.gsms.gsms.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return taskMapper.selectById(id);
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
    public boolean createTask(Task task) {
        return taskMapper.insert(task) > 0;
    }

    @Override
    public boolean updateTask(Task task) {
        return taskMapper.update(task) > 0;
    }

    @Override
    public boolean deleteTask(Long id) {
        return taskMapper.deleteById(id) > 0;
    }
}