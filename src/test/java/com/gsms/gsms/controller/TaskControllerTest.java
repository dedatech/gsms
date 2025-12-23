package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.domain.enums.TaskStatus;
import com.gsms.gsms.domain.enums.TaskType;
import com.gsms.gsms.domain.enums.TaskPriority;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskUpdateReq;
import com.gsms.gsms.infra.config.JwtInterceptor;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;
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

    @MockBean
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;
    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setProjectId(1L);
        testTask.setTitle("测试任务");
        testTask.setDescription("这是一个测试任务");
        testTask.setType(TaskType.BUG);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setAssigneeId(1L);
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        testTask.setEstimateHours(new BigDecimal("8.00"));
        testTask.setCreateUserId(1L);
        
        // 生成测试用的JWT Token
        testToken = "test.jwt.token";
        
        // Mock JWT拦截器，让所有请求通过
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void testGetTaskById_Success() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(testTask);

        // When & Then
        mockMvc.perform(get("/api/tasks/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试任务"));
    }

    @Test
    void testGetTaskById_NotFound() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenThrow(new RuntimeException("任务不存在"));

        // When & Then
        mockMvc.perform(get("/api/tasks/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testGetTasksByProjectId() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByProjectId(1L)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/project/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("测试任务"));
    }

    @Test
    void testGetTasksByCondition() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByCondition(1L, 1L, TaskStatus.IN_PROGRESS.getCode())).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/search")
                .header("Authorization", "Bearer " + testToken)
                .param("projectId", "1")
                .param("assigneeId", "1")
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("测试任务"));
    }

    @Test
    void testCreateTask_Success() throws Exception {
        // Given
        TaskCreateReq req = new TaskCreateReq();
        req.setProjectId(1L);
        req.setTitle("测试任务");
        req.setDescription("这是一个测试任务");
        req.setType(TaskType.TASK);
        req.setPriority(TaskPriority.MEDIUM);
        req.setAssigneeId(1L);
        req.setStatus(TaskStatus.IN_PROGRESS);
        req.setEstimateHours(new BigDecimal("8.00"));
        
        when(taskService.createTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        try (MockedStatic<UserContext> mockedContext = mockStatic(UserContext.class)) {
            mockedContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            
            mockMvc.perform(post("/api/tasks")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("测试任务"));
        }
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        // Given
        TaskUpdateReq req = new TaskUpdateReq();
        req.setId(1L);
        req.setProjectId(1L);
        req.setTitle("测试任务");
        req.setDescription("这是一个测试任务");
        req.setType(TaskType.BUG);
        req.setPriority(TaskPriority.MEDIUM);
        req.setAssigneeId(1L);
        req.setStatus(TaskStatus.IN_PROGRESS);
        req.setEstimateHours(new BigDecimal("8.00"));
        
        when(taskService.updateTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        try (MockedStatic<UserContext> mockedContext = mockStatic(UserContext.class)) {
            mockedContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            
            mockMvc.perform(put("/api/tasks")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("任务删除成功"));
    }
}