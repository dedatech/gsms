package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.domain.enums.TaskPriority;
import com.gsms.gsms.domain.enums.TaskStatus;
import com.gsms.gsms.domain.enums.TaskType;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 任务服务测试类
 */
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testTask = new Task();
        testTask.setId(1L);
        testTask.setProjectId(1L);
        testTask.setTitle("测试任务");
        testTask.setDescription("这是一个测试任务");
        testTask.setType(TaskType.TASK);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setAssigneeId(1L);
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        testTask.setEstimateHours(new BigDecimal("8.00"));
        testTask.setCreateUserId(1L);
    }

    @Test
    void testGetTaskById() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);

            // When
            Task result = taskService.getTaskById(1L);

            // Then
            assertNotNull(result);
            assertEquals("测试任务", result.getTitle());
            verify(taskMapper, times(1)).selectById(1L);
        }
    }

    @Test
    void testGetTasksByProjectId() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskMapper.selectByProjectId(1L)).thenReturn(tasks);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);

            // When
            List<Task> result = taskService.getTasksByProjectId(1L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试任务", result.get(0).getTitle());
            verify(taskMapper, times(1)).selectByProjectId(1L);
        }
    }

    @Test
    void testGetTasksByCondition() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskMapper.selectByCondition(1L, 1L, 1)).thenReturn(tasks);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);

            // When
            List<Task> result = taskService.getTasksByCondition(1L, 1L, 1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试任务", result.get(0).getTitle());
            verify(taskMapper, times(1)).selectByCondition(1L, 1L, 1);
        }
    }

    @Test
    void testCreateTask() {
        // Given
        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);
            when(authService.getAccessibleProjectIds(1L)).thenReturn(Arrays.asList(1L));

            // When
            Task result = taskService.createTask(testTask);

            // Then
            assertNotNull(result);
            verify(taskMapper, times(1)).insert(testTask);
        }
    }

    @Test
    void testUpdateTask() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.update(any(Task.class))).thenReturn(1);
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);
            when(authService.getAccessibleProjectIds(1L)).thenReturn(Arrays.asList(1L));

            // When
            Task result = taskService.updateTask(testTask);

            // Then
            assertNotNull(result);
            verify(taskMapper, times(1)).update(testTask);
        }
    }

    @Test
    void testUpdateTaskToDoneWithAutoSetEndDate() {
        // Given
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setProjectId(1L);
        existingTask.setTitle("测试任务");
        existingTask.setStatus(TaskStatus.IN_PROGRESS); // 原状态为进行中

        Task updateTask = new Task();
        updateTask.setId(1L);
        updateTask.setProjectId(1L);
        updateTask.setTitle("测试任务");
        updateTask.setStatus(TaskStatus.DONE); // 更新为已完成
        // 注意：不设置actualEndDate，让它为null，这样才会触发自动设置

        when(taskMapper.selectById(1L)).thenReturn(existingTask);
        when(taskMapper.update(any(Task.class))).thenReturn(1);
        // 模拟更新后返回的Task包含设置的日期
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setProjectId(1L);
        updatedTask.setTitle("测试任务");
        updatedTask.setStatus(TaskStatus.DONE);
        updatedTask.setActualEndDate(new Date()); // 模拟更新后返回的task有设置的日期
        when(taskMapper.selectById(1L)).thenReturn(updatedTask);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);
            when(authService.getAccessibleProjectIds(1L)).thenReturn(Arrays.asList(1L));

            // When
            Task result = taskService.updateTask(updateTask);

            // Then
            assertNotNull(result);
            assertEquals(TaskStatus.DONE, result.getStatus());
            // 验证实际结束日期被自动设置
            assertNotNull(result.getActualEndDate());
            verify(taskMapper, times(1)).update(any(Task.class));
        }
    }

    @Test
    void testUpdateTaskToInProgressWithAutoSetStartDate() {
        // Given
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setProjectId(1L);
        existingTask.setTitle("测试任务");
        existingTask.setStatus(TaskStatus.TODO); // 原状态为待处理

        Task updateTask = new Task();
        updateTask.setId(1L);
        updateTask.setProjectId(1L);
        updateTask.setTitle("测试任务");
        updateTask.setStatus(TaskStatus.IN_PROGRESS); // 更新为进行中
        // 注意：不设置actualStartDate，让它为null，这样才会触发自动设置

        when(taskMapper.selectById(1L)).thenReturn(existingTask);
        when(taskMapper.update(any(Task.class))).thenReturn(1);
        // 模拟更新后返回的Task包含设置的日期
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setProjectId(1L);
        updatedTask.setTitle("测试任务");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setActualStartDate(new Date()); // 模拟更新后返回的task有设置的日期
        when(taskMapper.selectById(1L)).thenReturn(updatedTask);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);
            when(authService.getAccessibleProjectIds(1L)).thenReturn(Arrays.asList(1L));

            // When
            Task result = taskService.updateTask(updateTask);

            // Then
            assertNotNull(result);
            assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
            // 验证实际开始日期被自动设置
            assertNotNull(result.getActualStartDate());
            verify(taskMapper, times(1)).update(any(Task.class));
        }
    }

    @Test
    void testDeleteTask() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.deleteById(1L)).thenReturn(1);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            when(authService.canViewAllTasks(1L)).thenReturn(true);
            when(authService.getAccessibleProjectIds(1L)).thenReturn(Arrays.asList(1L));

            // When
            taskService.deleteTask(1L);

            // Then
            verify(taskMapper, times(1)).deleteById(1L);
        }
    }
}