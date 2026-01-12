package com.gsms.gsms.service.impl;

import com.gsms.gsms.dto.gantt.*;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.model.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.model.enums.errorcode.TaskErrorCode;
import com.gsms.gsms.model.entity.Iteration;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.repository.IterationMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.service.AuthService;
import com.gsms.gsms.service.CacheService;
import com.gsms.gsms.service.GanttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 甘特图服务实现类
 */
@Service
public class GanttServiceImpl implements GanttService {
    private static final Logger logger = LoggerFactory.getLogger(GanttServiceImpl.class);

    private final ProjectMapper projectMapper;
    private final IterationMapper iterationMapper;
    private final TaskMapper taskMapper;
    private final AuthService authService;
    private final CacheService cacheService;

    public GanttServiceImpl(ProjectMapper projectMapper, IterationMapper iterationMapper,
                           TaskMapper taskMapper, AuthService authService, CacheService cacheService) {
        this.projectMapper = projectMapper;
        this.iterationMapper = iterationMapper;
        this.taskMapper = taskMapper;
        this.authService = authService;
        this.cacheService = cacheService;
    }

    @Override
    public GanttDataResp getProjectGanttData(Long projectId, LocalDate startDate, LocalDate endDate) {
        logger.info("获取项目甘特图数据: projectId={}, startDate={}, endDate={}", projectId, startDate, endDate);

        Long currentUserId = com.gsms.gsms.infra.utils.UserContext.getCurrentUserId();

        // 鉴权 - 检查项目访问权限
        authService.checkProjectAccess(currentUserId, projectId);

        // 查询项目信息
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }

        // 查询项目下的所有迭代
        List<Iteration> iterations = iterationMapper.selectByProjectId(projectId);
        logger.info("查询到 {} 个迭代", iterations.size());

        // 查询项目下的所有任务
        List<Task> allTasks = taskMapper.selectByProjectId(projectId);
        logger.info("查询到 {} 个任务", allTasks.size());

        // 输出所有任务的信息（调试用）
        for (Task task : allTasks) {
            logger.info("任务: id={}, title={}, parentId={}, iterationId={}",
                    task.getId(), task.getTitle(), task.getParentId(), task.getIterationId());
        }

        // 构建甘特图任务树
        List<GanttTaskResp> ganttTasks = buildGanttTaskTree(project, iterations, allTasks, startDate, endDate);

        // TODO: 查询任务依赖关系（如果后续需要依赖关系功能，需要创建 task_link 表）
        List<GanttLinkResp> links = new ArrayList<>();

        GanttDataResp resp = new GanttDataResp();
        resp.setData(ganttTasks);
        resp.setLinks(links);

        // 输出构建的树形结构 JSON（用于调试）
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(resp);
            logger.info("甘特图数据结构:\n{}", json);
        } catch (Exception e) {
            logger.error("JSON 序列化失败", e);
        }

        logger.info("构建甘特图数据成功: 项目={}, 任务数={}", project.getName(), ganttTasks.size());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskDates(Long taskId, TaskDateUpdateReq req) {
        logger.info("更新任务时间: ganttTaskId={}, planStartDate={}, planEndDate={}",
                taskId, req.getPlanStartDate(), req.getPlanEndDate());

        // 将甘特图 ID 转换为数据库 ID
        Long dbTaskId = convertGanttIdToDbId(taskId);
        logger.info("转换后的数据库ID: {}", dbTaskId);

        // 验证日期
        if (req.getPlanEndDate().isBefore(req.getPlanStartDate())) {
            throw new BusinessException(ProjectErrorCode.PROJECT_DATE_INVALID);
        }

        Long currentUserId = com.gsms.gsms.infra.utils.UserContext.getCurrentUserId();

        // 检查任务是否存在
        Task existTask = taskMapper.selectById(dbTaskId);
        if (existTask == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }

        // 鉴权
        authService.checkProjectAccess(currentUserId, existTask.getProjectId());

        // 如果有父任务，检查子任务时间不能超出父任务范围
        if (existTask.getParentId() != null) {
            Task parentTask = taskMapper.selectById(existTask.getParentId());
            if (parentTask != null) {
                if (req.getPlanStartDate().isBefore(parentTask.getPlanStartDate()) ||
                    req.getPlanEndDate().isAfter(parentTask.getPlanEndDate())) {
                    throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
                }
            }
        }

        // 更新任务
        Task task = new Task();
        task.setId(dbTaskId);
        task.setPlanStartDate(req.getPlanStartDate());
        task.setPlanEndDate(req.getPlanEndDate());
        task.setUpdateUserId(currentUserId);

        int result = taskMapper.update(task);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
        }

        logger.info("任务时间更新成功: taskId={}", dbTaskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskParent(Long taskId, Long newParentId) {
        logger.info("更新任务层级: ganttTaskId={}, newGanttParentId={}", taskId, newParentId);

        // 将甘特图 ID 转换为数据库 ID
        Long dbTaskId = convertGanttIdToDbId(taskId);
        Long dbNewParentId = newParentId != null ? convertGanttIdToDbId(newParentId) : null;

        logger.info("转换后: dbTaskId={}, dbNewParentId={}", dbTaskId, dbNewParentId);

        Long currentUserId = com.gsms.gsms.infra.utils.UserContext.getCurrentUserId();

        // 检查任务是否存在
        Task task = taskMapper.selectById(dbTaskId);
        if (task == null) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
        }

        // 鉴权
        authService.checkProjectAccess(currentUserId, task.getProjectId());

        // 不能将任务设置为自己的子任务
        if (dbNewParentId != null && dbNewParentId.equals(dbTaskId)) {
            throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
        }

        // 如果有新的父任务，验证父任务存在且在同一项目下
        if (dbNewParentId != null) {
            Task newParentTask = taskMapper.selectById(dbNewParentId);
            if (newParentTask == null) {
                throw new BusinessException(TaskErrorCode.TASK_NOT_FOUND);
            }
            if (!newParentTask.getProjectId().equals(task.getProjectId())) {
                throw new BusinessException(TaskErrorCode.TASK_PROJECT_INVALID);
            }

            // 检查是否会形成循环依赖
            if (willCreateCycle(dbTaskId, dbNewParentId)) {
                throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
            }

            // 检查任务时间是否在父任务范围内
            if (newParentTask.getPlanStartDate() != null && newParentTask.getPlanEndDate() != null) {
                if (task.getPlanStartDate() != null && task.getPlanStartDate().isBefore(newParentTask.getPlanStartDate())) {
                    throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
                }
                if (task.getPlanEndDate() != null && task.getPlanEndDate().isAfter(newParentTask.getPlanEndDate())) {
                    throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
                }
            }
        }

        // 更新父任务
        Task updateTask = new Task();
        updateTask.setId(dbTaskId);
        updateTask.setParentId(dbNewParentId);
        updateTask.setUpdateUserId(currentUserId);

        int result = taskMapper.update(updateTask);
        if (result <= 0) {
            throw new BusinessException(TaskErrorCode.TASK_UPDATE_FAILED);
        }

        logger.info("任务层级更新成功: taskId={}, newParentId={}", dbTaskId, dbNewParentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTaskLink(TaskLinkCreateReq req) {
        logger.info("创建任务依赖关系: source={}, target={}, type={}",
                req.getSource(), req.getTarget(), req.getType());

        // TODO: 需要创建 task_link 表来存储任务依赖关系
        // 目前暂不实现，返回成功
        logger.warn("任务依赖关系功能暂未实现，需要创建 task_link 表");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskLink(Long linkId) {
        logger.info("删除任务依赖关系: linkId={}", linkId);

        // TODO: 需要创建 task_link 表来存储任务依赖关系
        // 目前暂不实现，返回成功
        logger.warn("任务依赖关系功能暂未实现，需要创建 task_link 表");
    }

    // ========== 私有方法：构建树形结构 ==========

    /**
     * 构建甘特图任务树
     * 结构：项目 -> 迭代 -> 任务 -> 子任务
     */
    private List<GanttTaskResp> buildGanttTaskTree(Project project, List<Iteration> iterations,
                                                     List<Task> allTasks, LocalDate startDate, LocalDate endDate) {
        List<GanttTaskResp> result = new ArrayList<>();

        // 1. 创建项目根节点
        GanttTaskResp projectNode = convertProjectToGanttTask(project, null);
        result.add(projectNode);

        // 2. 按日期范围筛选迭代
        List<Iteration> filteredIterations = filterByDateRange(iterations, startDate, endDate);

        // 3. 按日期范围筛选任务（顶级任务，parentId 为 null）
        List<Task> topLevelTasks = allTasks.stream()
                .filter(t -> t.getParentId() == null)
                .collect(Collectors.toList());
        topLevelTasks = filterByDateRange(topLevelTasks, startDate, endDate);

        // 4. 为每个迭代创建子节点
        List<GanttTaskResp> iterationNodes = new ArrayList<>();
        for (Iteration iteration : filteredIterations) {
            logger.info("处理迭代: id={}, name={}", iteration.getId(), iteration.getName());
            GanttTaskResp iterationNode = convertIterationToGanttTask(iteration, project.getId());
            logger.info("迭代节点: id={}, parent={}", iterationNode.getId(), iterationNode.getParent());

            // 查询该迭代下的任务（parentId 为 null 的任务）
            List<Task> iterationTasks = allTasks.stream()
                    .filter(t -> iteration.getId().equals(t.getIterationId()) && t.getParentId() == null)
                    .collect(Collectors.toList());
            logger.info("迭代 {} 下有 {} 个顶级任务", iteration.getName(), iterationTasks.size());

            // 递归构建任务树
            for (Task task : iterationTasks) {
                GanttTaskResp taskNode = convertTaskToGanttTask(task, iterationNode.getGanttId());
                logger.info("任务节点: id={}, ganttId={}, parent={}, text={}", taskNode.getId(), taskNode.getGanttId(), taskNode.getParent(), taskNode.getText());
                buildSubtaskTree(taskNode, task, allTasks);
                iterationNode.getSubtasks().add(taskNode);
            }

            iterationNodes.add(iterationNode);
        }

        // 5. 添加不在迭代中的任务（直接属于项目）
        List<Task> orphanTasks = topLevelTasks.stream()
                .filter(t -> t.getIterationId() == null)
                .collect(Collectors.toList());
        for (Task task : orphanTasks) {
            GanttTaskResp taskNode = convertTaskToGanttTask(task, projectNode.getGanttId());
            buildSubtaskTree(taskNode, task, allTasks);
            iterationNodes.add(taskNode);
        }

        // 将迭代节点添加到项目节点下
        projectNode.setSubtasks(iterationNodes);

        return result;
    }

    /**
     * 递归构建子任务树
     */
    private void buildSubtaskTree(GanttTaskResp parentNode, Task parentTask, List<Task> allTasks) {
        List<Task> subtasks = allTasks.stream()
                .filter(t -> parentTask.getId().equals(t.getParentId()))
                .collect(Collectors.toList());

        logger.info("构建子任务树: 父任务id={}, 父任务名称={}, 找到{}个子任务",
                parentTask.getId(), parentTask.getTitle(), subtasks.size());

        for (Task subtask : subtasks) {
            logger.info("  - 子任务: id={}, title={}, parentId={}, iterationId={}",
                    subtask.getId(), subtask.getTitle(), subtask.getParentId(), subtask.getIterationId());
            GanttTaskResp subtaskNode = convertTaskToGanttTask(subtask, parentNode.getGanttId());
            buildSubtaskTree(subtaskNode, subtask, allTasks);
            parentNode.getSubtasks().add(subtaskNode);
        }
    }

    /**
     * 将项目转换为甘特图任务节点
     */
    private GanttTaskResp convertProjectToGanttTask(Project project, String prefix) {
        GanttTaskResp node = new GanttTaskResp();
        // id 保持原始数据库ID
        node.setId(project.getId());
        // 项目使用正数 ID 作为 ganttId
        node.setGanttId(project.getId());
        node.setText(project.getName());
        node.setType("project");
        node.setStartDate(project.getPlanStartDate());
        node.setEndDate(project.getPlanEndDate());
        node.setDuration(calculateDuration(project.getPlanStartDate(), project.getPlanEndDate()));
        node.setProgress(0.0);
        node.setParent(null);
        node.setStatus(project.getStatus() != null ? project.getStatus().name() : null);

        // 项目经理信息
        if (project.getManagerId() != null) {
            node.setOwnerId(project.getManagerId());
            String managerName = cacheService.getUserNicknameById(project.getManagerId());
            node.setOwner(managerName);
        }

        node.setActualStartDate(project.getActualStartDate());
        node.setActualEndDate(project.getActualEndDate());
        node.setSubtasks(new ArrayList<>());
        return node;
    }

    /**
     * 将迭代转换为甘特图任务节点
     */
    private GanttTaskResp convertIterationToGanttTask(Iteration iteration, Long projectId) {
        GanttTaskResp node = new GanttTaskResp();
        // id 保持原始数据库ID
        node.setId(iteration.getId());
        // 迭代使用负数 ID 作为 ganttId：-1000000 - iterationId
        node.setGanttId(-1000000L - iteration.getId());
        node.setText(iteration.getName());
        node.setType("iteration");
        node.setStartDate(iteration.getPlanStartDate());
        node.setEndDate(iteration.getPlanEndDate());
        node.setDuration(calculateDuration(iteration.getPlanStartDate(), iteration.getPlanEndDate()));
        node.setProgress(0.0);
        node.setParent(projectId); // 设置父节点为项目 ID
        node.setStatus(iteration.getStatus() != null ? iteration.getStatus().name() : null);
        node.setSubtasks(new ArrayList<>());
        return node;
    }

    /**
     * 将任务转换为甘特图任务节点
     */
    private GanttTaskResp convertTaskToGanttTask(Task task, Long parentId) {
        GanttTaskResp node = new GanttTaskResp();
        // id 保持原始数据库ID
        node.setId(task.getId());
        // 任务使用负数 ID 作为 ganttId：-2000000 - taskId
        node.setGanttId(-2000000L - task.getId());
        node.setText(task.getTitle());
        node.setType("task");
        node.setStartDate(task.getPlanStartDate());
        node.setEndDate(task.getPlanEndDate());
        node.setDuration(calculateDuration(task.getPlanStartDate(), task.getPlanEndDate()));
        node.setProgress(0.0);
        node.setParent(parentId); // 使用传入的父节点 ID
        node.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        node.setPriority(task.getPriority() != null ? task.getPriority().name() : null);

        // 负责人信息
        if (task.getAssigneeId() != null) {
            node.setOwnerId(task.getAssigneeId());
            String assigneeName = cacheService.getUserNicknameById(task.getAssigneeId());
            node.setOwner(assigneeName);
        }

        node.setActualStartDate(task.getActualStartDate());
        node.setActualEndDate(task.getActualEndDate());
        node.setSubtasks(new ArrayList<>());
        return node;
    }

    /**
     * 计算工期（天数）
     */
    private Integer calculateDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return (int) days + 1; // 包含开始和结束当天
    }

    /**
     * 将甘特图 ID 转换为数据库 ID
     * 项目：正数 ID (如 2) -> 数据库 ID 2
     * 迭代：-1000000 - id (如 -1000001) -> 数据库 ID 1
     * 任务：-2000000 - id (如 -2000001) -> 数据库 ID 1
     */
    private Long convertGanttIdToDbId(Long ganttId) {
        if (ganttId == null) {
            return null;
        }
        if (ganttId > 0) {
            // 正数 ID 是项目 ID，直接返回
            return ganttId;
        } else if (ganttId <= -1000000 && ganttId > -2000000) {
            // 迭代 ID：-1000000 - id
            return -1000000L - ganttId;
        } else if (ganttId <= -2000000) {
            // 任务 ID：-2000000 - id
            return -2000000L - ganttId;
        }
        return ganttId;
    }

    /**
     * 按日期范围筛选
     */
    private <T> List<T> filterByDateRange(List<T> items, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return items;
        }

        // 简化处理：如果不指定日期范围，返回所有
        // TODO: 实现更精确的日期范围筛选
        return items;
    }

    /**
     * 检查是否会形成循环依赖
     */
    private boolean willCreateCycle(Long taskId, Long newParentId) {
        Set<Long> visited = new HashSet<>();
        Long currentId = newParentId;

        while (currentId != null) {
            if (currentId.equals(taskId)) {
                return true; // 形成循环
            }
            if (visited.contains(currentId)) {
                return true; // 检测到循环
            }
            visited.add(currentId);
            Task task = taskMapper.selectById(currentId);
            if (task == null) {
                break;
            }
            currentId = task.getParentId();
        }

        return false;
    }
}
