package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.model.enums.ProjectType;
import com.gsms.gsms.dto.task.TaskInfoResp;
import com.gsms.gsms.dto.task.TaskQueryReq;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskUpdateReq;
import com.gsms.gsms.dto.task.TaskStatusUpdateReq;
import com.gsms.gsms.dto.task.TaskConverter;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.model.enums.errorcode.TaskErrorCode;
import com.gsms.gsms.model.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.TaskService;
import com.gsms.gsms.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final AuthService authService;
    private final CacheService cacheService;

    public TaskServiceImpl(TaskMapper taskMapper, ProjectMapper projectMapper,
                           ProjectMemberMapper projectMemberMapper,
                           AuthService authService, CacheService cacheService) {
        this.taskMapper = taskMapper;
        this.projectMapper = projectMapper;
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
        logger.info("根据条件分页查询任务: projectId={}, assigneeId={}, status={}, pageNum={}, pageSize={}",
                    taskQueryReq.getProjectId(), taskQueryReq.getAssigneeId(),
                    taskQueryReq.getStatus(), taskQueryReq.getPageNum(), taskQueryReq.getPageSize());

        Long currentUserId = UserContext.getCurrentUserId();
        Long projectId = taskQueryReq.getProjectId();
        Long assigneeId = taskQueryReq.getAssigneeId();
        TaskStatus status = taskQueryReq.getStatus();
        Integer statusCode = status != null ? status.getCode() : null;
        Integer pageNum = taskQueryReq.getPageNum() != null ? taskQueryReq.getPageNum() : 1;
        Integer pageSize = taskQueryReq.getPageSize() != null ? taskQueryReq.getPageSize() : 10;

        // 权限判断
        if (projectId == null && !authService.canViewAllTasks(currentUserId)) {
            logger.warn("用户无全局权限且未指定项目ID，拒绝查询");
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        // 1. 查询所有符合筛选条件的任务（可能是子任务）
        logger.info("步骤1: 查询所有符合筛选条件的任务");
        List<Task> matchedTasks = taskMapper.selectByCondition(projectId, assigneeId, statusCode);
        logger.info("符合筛选条件的任务数量: {}", matchedTasks.size());

        // 2. 如果没有任务，直接返回空结果
        if (matchedTasks.isEmpty()) {
            logger.info("没有符合条件的任务，返回空结果");
            return PageResult.success(new ArrayList<>(), 0L, pageNum, 0);
        }

        // 3. 提取所有相关的父任务ID（包括：符合条件的父任务 + 子任务的父任务）
        Set<Long> parentIds = new HashSet<>();
        for (Task task : matchedTasks) {
            if (task.getParentId() == null) {
                // 是父任务，直接加入
                parentIds.add(task.getId());
            } else {
                // 是子任务，加入其父任务ID
                parentIds.add(task.getParentId());
            }
        }
        logger.info("相关父任务ID集合: {}", parentIds);

        // 4. 查询父任务（分页）
        List<Long> parentIdList = new ArrayList<>(parentIds);
        PageHelper.startPage(pageNum, pageSize);
        List<Task> parentTasks = taskMapper.selectTasksByIds(parentIdList);
        PageInfo<Task> pageInfo = new PageInfo<>(parentTasks);
        long total = pageInfo.getTotal();
        logger.info("步骤2: 分页查询父任务，本页返回{}个，总计{}个父任务", parentTasks.size(), total);

        // 5. 如果没有父任务，直接返回空结果
        if (parentTasks.isEmpty()) {
            logger.info("本页没有父任务，返回空结果");
            return PageResult.success(new ArrayList<>(), total, pageNum, 0);
        }

        // 6. 获取本页父任务的所有子任务
        List<Long> pagedParentIds = parentTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        List<Task> allSubtasks = taskMapper.selectSubtasksByParentIds(pagedParentIds);
        logger.info("步骤3: 查询到{}个子任务", allSubtasks.size());

        // 7. 合并父任务和子任务（使用 Set 去重）
        Set<Long> taskIds = new HashSet<>();
        List<Task> allTasks = new ArrayList<>();
        for (Task task : parentTasks) {
            if (taskIds.add(task.getId())) {
                allTasks.add(task);
            }
        }
        for (Task task : allSubtasks) {
            if (taskIds.add(task.getId())) {
                allTasks.add(task);
            }
        }
        logger.info("步骤4: 合并后的任务总数（去重后）: {}", allTasks.size());

        // 8. 使用后端已有方法构建树形结构
        List<TaskInfoResp> respList = TaskInfoResp.buildTree(allTasks);
        logger.info("步骤5: 构建树形结构，返回{}个根任务", respList.size());

        // 9. 扁平化树形结构（用于填充用户信息）
        List<TaskInfoResp> flatList = flattenTree(respList);
        logger.info("扁平化后任务总数: {}", flatList.size());

        // 10. 批量填充用户信息
        enrichTaskInfoRespList(flatList);

        // 11. 返回分页结果
        logger.info("查询完成: total={}（父任务数）, pageNum={}, pageSize={}, 返回根任务数={}",
                    total, pageNum, pageSize, respList.size());
        return PageResult.success(respList, total, pageNum, Integer.valueOf(respList.size()));
    }

    /**
     * 扁平化树形结构（递归）
     * @param tree 树形结构的任务列表
     * @return 扁平化的任务列表
     */
    private List<TaskInfoResp> flattenTree(List<TaskInfoResp> tree) {
        List<TaskInfoResp> result = new ArrayList<>();
        for (TaskInfoResp task : tree) {
            result.add(task);
            if (task.getSubtasks() != null && !task.getSubtasks().isEmpty()) {
                result.addAll(flattenTree(task.getSubtasks()));
            }
        }
        return result;
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

        // 获取项目信息，校验项目类型和迭代关联
        Project project = projectMapper.selectById(task.getProjectId());
        if (project == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }

        // 注释：取消项目类型和迭代关联的约束
        // 所有项目都可以创建任务，迭代字段为可选
        // 需求可以先不规划到迭代，后续通过规划功能关联

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

    @Override
    public java.util.List<TaskInfoResp> getSubtasks(Long parentId) {
        logger.debug("获取子任务列表: parentId={}", parentId);

        Long currentUserId = UserContext.getCurrentUserId();

        // 鉴权 - 检查父任务是否存在且用户有访问权限
        // 系统管理员和业务相关角色可以访问所有任务
        Task parentTask;
        if (authService.canViewAllTasks(currentUserId)) {
            parentTask = taskMapper.selectById(parentId);
        } else {
            // 普通用户：通过SQL JOIN验证权限
            parentTask = taskMapper.selectByIdForUser(parentId, currentUserId);
        }

        if (parentTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }

        // 查询子任务
        List<Task> subtasks;
        if (authService.canViewAllTasks(currentUserId)) {
            // 全局权限用户：查询所有子任务
            subtasks = taskMapper.selectByCondition(parentTask.getProjectId(), null, null)
                    .stream()
                    .filter(t -> parentId.equals(t.getParentId()))
                    .collect(Collectors.toList());
        } else {
            // 普通用户：只能查询用户可访问项目的子任务
            subtasks = taskMapper.selectSubtasks(parentId, currentUserId);
        }

        // 转换为 DTO
        List<TaskInfoResp> respList = TaskInfoResp.from(subtasks);

        // 批量填充用户信息
        enrichTaskInfoRespList(respList);

        logger.info("获取子任务列表成功: parentId={}, count={}", parentId, subtasks.size());
        return respList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task updateIterationId(Long taskId, Long iterationId) {
        logger.info("更新任务迭代ID: taskId={}, iterationId={}", taskId, iterationId);

        // 检查任务是否存在
        Task existTask = taskMapper.selectById(taskId);
        if (existTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }

        // 鉴权 - 检查项目访问权限
        Long currentUserId = UserContext.getCurrentUserId();
        authService.checkProjectAccess(currentUserId, existTask.getProjectId());

        // 如果设置了迭代ID，需要校验迭代是否存在且属于同一项目
        if (iterationId != null) {
            // 这里可以添加迭代的校验逻辑，暂时跳过
            // 因为 iterationId 是外键，数据库会保证一致性
        }

        // 创建更新实体
        Task task = new Task();
        task.setId(taskId);
        task.setIterationId(iterationId);
        task.setUpdateUserId(currentUserId);

        int result = taskMapper.updateIterationId(task);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
        }

        logger.info("任务迭代ID更新成功: taskId={}, iterationId={}", taskId, iterationId);
        return taskMapper.selectById(taskId);
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