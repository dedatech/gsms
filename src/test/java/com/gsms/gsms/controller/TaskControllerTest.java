package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.entity.Task;
import com.gsms.gsms.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 任务控制器测试类
 */
@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;

    @BeforeEach
    void setUp() {
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
    void testGetTaskById_Success() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(testTask);

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试任务"));
    }

    @Test
    void testGetTaskById_NotFound() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("任务不存在"));
    }

    @Test
    void testGetTasksByProjectId() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByProjectId(1L)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("测试任务"));
    }

    @Test
    void testGetTasksByCondition() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByCondition(1L, 1L, 1)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/search")
                .param("projectId", "1")
                .param("assigneeId", "1")
                .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("测试任务"));
    }

    @Test
    void testCreateTask_Success() throws Exception {
        // Given
        when(taskService.createTask(any(Task.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务创建成功"));
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        // Given
        when(taskService.updateTask(any(Task.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务更新成功"));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        // Given
        when(taskService.deleteTask(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务删除成功"));
    }
}