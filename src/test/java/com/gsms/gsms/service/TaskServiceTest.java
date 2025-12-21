package com.gsms.gsms.service;

import com.gsms.gsms.entity.Task;
import com.gsms.gsms.mapper.TaskMapper;
import com.gsms.gsms.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
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
        testTask.setType(1);
        testTask.setPriority(2);
        testTask.setAssigneeId(1L);
        testTask.setStatus(1);
        testTask.setEstimateHours(new BigDecimal("8.00"));
        testTask.setCreateUserId(1L);
    }

    @Test
    void testGetTaskById() {
        // Given
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        // When
        Task result = taskService.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals("测试任务", result.getTitle());
        verify(taskMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetTasksByProjectId() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskMapper.selectByProjectId(1L)).thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasksByProjectId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试任务", result.get(0).getTitle());
        verify(taskMapper, times(1)).selectByProjectId(1L);
    }

    @Test
    void testGetTasksByCondition() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskMapper.selectByCondition(1L, 1L, 1)).thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasksByCondition(1L, 1L, 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试任务", result.get(0).getTitle());
        verify(taskMapper, times(1)).selectByCondition(1L, 1L, 1);
    }

    @Test
    void testCreateTask() {
        // Given
        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        // When
        boolean result = taskService.createTask(testTask);

        // Then
        assertTrue(result);
        verify(taskMapper, times(1)).insert(testTask);
    }

    @Test
    void testUpdateTask() {
        // Given
        when(taskMapper.update(any(Task.class))).thenReturn(1);

        // When
        boolean result = taskService.updateTask(testTask);

        // Then
        assertTrue(result);
        verify(taskMapper, times(1)).update(testTask);
    }

    @Test
    void testDeleteTask() {
        // Given
        when(taskMapper.deleteById(1L)).thenReturn(1);

        // When
        boolean result = taskService.deleteTask(1L);

        // Then
        assertTrue(result);
        verify(taskMapper, times(1)).deleteById(1L);
    }
}