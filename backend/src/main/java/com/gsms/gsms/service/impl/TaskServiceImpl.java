package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.dto.task.TaskInfoResp;
import com.gsms.gsms.dto.task.TaskQueryReq;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskUpdateReq;
import com.gsms.gsms.dto.task.TaskStatusUpdateReq;
import com.gsms.gsms.dto.task.TaskConverter;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.model.enums.errorcode.TaskErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.TaskService;
import com.gsms.gsms.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskMapper taskMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final AuthService authService;
    private final CacheService cacheService;

    public TaskServiceImpl(TaskMapper taskMapper, ProjectMemberMapper projectMemberMapper,
                           AuthService authService, CacheService cacheService) {
        this.taskMapper = taskMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.authService = authService;
        this.cacheService = cacheService;
    }

    @Override
    public Task getById(Long id) {
        logger.debug("根据ID查询任务: {}", id);
        // 先鉴权
        Long currentUserId = UserContext.getCurrentUserId();

        // 系统管理员和业务相关角色可以访问所有任务
        if (authService.canViewAllTasks(currentUserId)) {
            Task task = taskMapper.selectById(id);
            if (task == null) {
                throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
            }
            return task;
        }

        // 普通用户：通过SQL JOIN验证权限，查询任务是否存在且用户有访问权限
        Task task = taskMapper.selectByIdForUser(id, currentUserId);
        if (task == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    @Override
    public PageResult<TaskInfoResp> findAll(TaskQueryReq taskQueryReq) {
        logger.debug("根据条件分页查询任务: taskQueryReq={}", taskQueryReq);

        Long currentUserId = UserContext.getCurrentUserId();
        Long projectId = taskQueryReq.getProjectId();
        Long assigneeId = taskQueryReq.getAssigneeId();
        TaskStatus status = taskQueryReq.getStatus();
        Integer statusCode = status != null ? status.getCode() : null;

        // 在Service层处理分页逻辑
        PageHelper.startPage(taskQueryReq.getPageNum(), taskQueryReq.getPageSize());

        List<Task> tasks;
        // 系统管理员和业务相关角色可以查看所有任务
        if (authService.canViewAllTasks(currentUserId)) {
            tasks = taskMapper.selectByCondition(projectId, assigneeId, statusCode);
        } else {
            // 普通用户只查询自己参与项目下的任务（在SQL层过滤）
            tasks = taskMapper.selectAccessibleTasksByCondition(currentUserId, projectId, assigneeId, statusCode);
        }

        // 转换为响应DTO
        PageInfo<Task> pageInfo = new PageInfo<>(tasks);
        List<TaskInfoResp> respList = TaskInfoResp.from(tasks);

        // 使用缓存填充创建人、更新人信息
        enrichTaskInfoRespList(respList);

        // 返回分页结果
        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task create(TaskCreateReq createReq) {
        logger.info("创建任务: {}", createReq.getTitle());

        // DTO转Entity
        Task task = TaskConverter.toTask(createReq);

        // 先鉴权 - 检查项目访问权限
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, task.getProjectId());

        // 校验任务负责人必须为项目成员（如果指定了负责人）
        if (task.getAssigneeId() != null) {
            List<Long> memberUserIds = projectMemberMapper.selectUserIdsByProjectId(task.getProjectId());
            if (memberUserIds == null || memberUserIds.isEmpty() || !memberUserIds.contains(task.getAssigneeId())) {
                throw new BusinessException(TaskErrorCode.TASK_ASSIGNEE_INVALID);
            }
        }

        // 设置默认状态为待办
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        task.setCreateUserId(currentUserId);
        task.setUpdateUserId(currentUserId);

        int result = taskMapper.insert(task);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_CREATE_FAILED);
        }

        logger.info("任务创建成功: {}", task.getTitle());
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task update(TaskUpdateReq updateReq) {
        logger.info("更新任务: {}", updateReq.getId());
        // 检查任务是否存在
        Task existTask = taskMapper.selectById(updateReq.getId());
        if (existTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }

        // 鉴权 - 检查项目访问权限
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, existTask.getProjectId());

        // DTO转Entity
        Task task = TaskConverter.toTask(updateReq);

        // 如有修改负责人，需校验其为项目成员
        if (task.getAssigneeId() != null) {
            List<Long> memberUserIds = projectMemberMapper.selectUserIdsByProjectId(existTask.getProjectId());
            if (memberUserIds == null || memberUserIds.isEmpty() || !memberUserIds.contains(task.getAssigneeId())) {
                throw new BusinessException(TaskErrorCode.TASK_ASSIGNEE_INVALID);
            }
        }

        // 如果任务状态更新为已完成且实际结束日期未设置，则自动设置实际结束日期
        if (task.getStatus() != null &&
            task.getStatus() == TaskStatus.DONE &&
            existTask.getStatus() != TaskStatus.DONE &&
            task.getActualEndDate() == null) {
            task.setActualEndDate(LocalDate.now());
        }

        // 如果任务状态更新为进行中且实际开始日期未设置，则自动设置实际开始日期
        if (task.getStatus() != null &&
            task.getStatus() == TaskStatus.IN_PROGRESS &&
            (existTask.getStatus() == TaskStatus.TODO || existTask.getStatus() == null) &&
            task.getActualStartDate() == null) {
            task.setActualStartDate(LocalDate.now());
        }

        task.setUpdateUserId(currentUserId);

        int result = taskMapper.update(task);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
        }

        logger.info("任务更新成功: {}", task.getId());
        return taskMapper.selectById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task updateStatus(TaskStatusUpdateReq updateReq) {
        logger.info("更新任务状态: taskId={}, status={}", updateReq.getId(), updateReq.getStatus());

        // 检查任务是否存在
        Task existTask = taskMapper.selectById(updateReq.getId());
        if (existTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }

        // 鉴权 - 检查项目访问权限
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, existTask.getProjectId());

        TaskStatus newStatus = updateReq.getStatus();
        TaskStatus oldStatus = existTask.getStatus();

        // 创建更新实体
        Task task = new Task();
        task.setId(updateReq.getId());
        task.setStatus(newStatus);
        task.setUpdateUserId(currentUserId);

        // 处理实际开始时间
        LocalDate actualStartDate = null;
        if (updateReq.getActualStartDate() != null) {
            // 用户手动提供，优先使用
            actualStartDate = updateReq.getActualStartDate();
        } else {
            // 根据状态转换自动设置
            if (newStatus == TaskStatus.IN_PROGRESS && oldStatus != TaskStatus.IN_PROGRESS) {
                // TODO/DONE → IN_PROGRESS：设置实际开始时间
                actualStartDate = LocalDate.now();
            } else if (newStatus == TaskStatus.DONE && oldStatus == TaskStatus.TODO) {
                // TODO → DONE（跳跃）：设置实际开始时间
                actualStartDate = LocalDate.now();
            } else if (newStatus == TaskStatus.TODO && oldStatus == TaskStatus.IN_PROGRESS) {
                // IN_PROGRESS → TODO：清空实际开始时间
                actualStartDate = null;
            } else if (newStatus == TaskStatus.TODO && oldStatus == TaskStatus.DONE) {
                // DONE → TODO（跳跃）：清空实际开始时间
                actualStartDate = null;
            } else {
                // 其他情况保持原值
                actualStartDate = existTask.getActualStartDate();
            }
        }
        task.setActualStartDate(actualStartDate);

        // 处理实际结束时间
        LocalDate actualEndDate = null;
        if (updateReq.getActualEndDate() != null) {
            // 用户手动提供，优先使用
            actualEndDate = updateReq.getActualEndDate();
        } else {
            // 根据状态转换自动设置
            if (newStatus == TaskStatus.DONE && oldStatus != TaskStatus.DONE) {
                // TODO/IN_PROGRESS → DONE：设置实际结束时间
                actualEndDate = LocalDate.now();
            } else if (newStatus != TaskStatus.DONE && oldStatus == TaskStatus.DONE) {
                // DONE → TODO/IN_PROGRESS：清空实际结束时间（重新打开任务）
                actualEndDate = null;
            } else {
                // 其他情况保持原值
                actualEndDate = existTask.getActualEndDate();
            }
        }
        task.setActualEndDate(actualEndDate);

        int result = taskMapper.updateStatus(task);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
        }

        logger.info("任务状态更新成功: {}, 实际开始时间={}, 实际结束时间={}",
                    task.getId(), task.getActualStartDate(), task.getActualEndDate());
        return taskMapper.selectById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        logger.info("删除任务: {}", id);
        // 检查任务是否存在
        Task existTask = taskMapper.selectById(id);
        if (existTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }

        // 鉴权 - 检查项目访问权限
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, existTask.getProjectId());

        int result = taskMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_DELETE_FAILED);
        }

        logger.info("任务删除成功: {}", id);
    }

    // ========== 内部方法：数据填充 ==========

    /**
     * 填充单个 TaskInfoResp 的创建人、更新人、负责人信息
     */
    private void enrichTaskInfoResp(TaskInfoResp resp) {
        if (resp.getCreateUserId() != null) {
            String creatorName = cacheService.getUserNicknameById(resp.getCreateUserId());
            resp.setCreateUserName(creatorName);
        }
        if (resp.getUpdateUserId() != null) {
            String updaterName = cacheService.getUserNicknameById(resp.getUpdateUserId());
            resp.setUpdateUserName(updaterName);
        }
        if (resp.getAssigneeId() != null) {
            String assigneeName = cacheService.getUserNicknameById(resp.getAssigneeId());
            resp.setAssigneeName(assigneeName);
        }
    }

    /**
     * 批量填充 TaskInfoResp 列表的创建人、更新人信息
     */
    private void enrichTaskInfoRespList(List<TaskInfoResp> respList) {
        if (respList == null || respList.isEmpty()) {
            return;
        }
        for (TaskInfoResp resp : respList) {
            enrichTaskInfoResp(resp);
        }
    }
}