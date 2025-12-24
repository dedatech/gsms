package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.domain.enums.errorcode.TaskErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    private AuthService authService;

    @Override
    public Task getTaskById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }
    
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        // 系统管理员和业务相关角色可以查看所有任务
        if (!authService.canViewAllTasks(currentUserId)) {
            List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
            if (projectIds == null || projectIds.isEmpty()) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
            if (task.getProjectId() == null || !projectIds.contains(task.getProjectId())) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
    
        return task;
    }

    @Override
    public List<Task> getTasksByProjectId(Long projectId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        // 系统管理员和业务相关角色可以查看所有项目的任务
        if (!authService.canViewAllTasks(currentUserId)) {
            List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
            if (projectIds == null || projectIds.isEmpty() || !projectIds.contains(projectId)) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
        return taskMapper.selectByProjectId(projectId);
    }

    @Override
    public List<Task> getTasksByCondition(Long projectId, Long assigneeId, Integer status) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    
        List<Task> tasks = taskMapper.selectByCondition(projectId, assigneeId, status);
        // 系统管理员和业务相关角色可以查看所有任务
        if (authService.canViewAllTasks(currentUserId)) {
            return tasks;
        }
    
        List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
        if (projectIds == null || projectIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Set<Long> idSet = new HashSet<>(projectIds);
        tasks.removeIf(t -> t.getProjectId() == null || !idSet.contains(t.getProjectId()));
        return tasks;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task createTask(Task task) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    
        // 非具备全局任务查看权限的用户只能在自己参与的项目中创建任务
        if (!authService.canViewAllTasks(currentUserId)) {
            List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
            if (projectIds == null || projectIds.isEmpty() || !projectIds.contains(task.getProjectId())) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
    
        // 校验任务负责人必须为项目成员（如果指定了负责人）
        if (task.getAssigneeId() != null) {
            List<Long> memberUserIds = projectMemberMapper.selectUserIdsByProjectId(task.getProjectId());
            if (memberUserIds == null || memberUserIds.isEmpty() || !memberUserIds.contains(task.getAssigneeId())) {
                throw new BusinessException(CommonErrorCode.PARAM_INVALID.getCode(), "任务负责人必须为项目成员");
            }
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
    
        // 非具备全局任务查看权限的用户只能修改自己成员项目下的任务
        if (!authService.canViewAllTasks(currentUserId)) {
            List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
            if (projectIds == null || projectIds.isEmpty() || !projectIds.contains(existTask.getProjectId())) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
    
        // 如有修改负责人，需校验其为项目成员
        if (task.getAssigneeId() != null) {
            List<Long> memberUserIds = projectMemberMapper.selectUserIdsByProjectId(existTask.getProjectId());
            if (memberUserIds == null || memberUserIds.isEmpty() || !memberUserIds.contains(task.getAssigneeId())) {
                throw new BusinessException(CommonErrorCode.PARAM_INVALID.getCode(), "任务负责人必须为项目成员");
            }
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
    
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    
        // 非具备全局任务查看权限的用户只能删除自己成员项目下的任务
        if (!authService.canViewAllTasks(currentUserId)) {
            List<Long> projectIds = authService.getAccessibleProjectIds(currentUserId);
            if (projectIds == null || projectIds.isEmpty() || !projectIds.contains(existTask.getProjectId())) {
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }
    
        int result = taskMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_DELETE_FAILED);
        }
    }
}