package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.TaskPriority;
import com.gsms.gsms.domain.enums.TaskStatus;
import com.gsms.gsms.domain.enums.TaskType;
import com.gsms.gsms.domain.enums.UserStatus;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskUpdateReq;
import com.gsms.gsms.dto.task.TaskInfoResp;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.TaskService;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 任务控制器集成测试类
 * 继承BaseControllerTest，使用真实Service和数据库
 */
public class TaskControllerTest extends BaseControllerTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private Task testTask;
    private User testUser;
    private User testUser2;
    private String testToken;
    private String testToken2;
    private Long testProjectId;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试用户1
        UserCreateReq userCreateReq = new UserCreateReq();
        userCreateReq.setUsername("tasktestuser");
        userCreateReq.setPassword("password123");
        userCreateReq.setNickname("任务测试用户");
        userCreateReq.setEmail("tasktest@example.com");
        userCreateReq.setPhone("13800138100");
        UserInfoResp userResp = userService.create(userCreateReq);

        testUser = new User();
        testUser.setId(userResp.getId());
        testUser.setUsername(userResp.getUsername());
        testUser.setPassword("password123");
        testUser.setNickname(userResp.getNickname());
        testUser.setEmail(userResp.getEmail());
        testUser.setPhone(userResp.getPhone());
        testUser.setStatus(userResp.getStatus());
        testToken = JwtUtil.generateToken(testUser.getId(), testUser.getUsername());

        // 创建测试用户2
        UserCreateReq userCreateReq2 = new UserCreateReq();
        userCreateReq2.setUsername("tasktestuser2");
        userCreateReq2.setPassword("password123");
        userCreateReq2.setNickname("任务测试用户2");
        userCreateReq2.setEmail("tasktest2@example.com");
        userCreateReq2.setPhone("13800138101");
        UserInfoResp userResp2 = userService.create(userCreateReq2);

        testUser2 = new User();
        testUser2.setId(userResp2.getId());
        testUser2.setUsername(userResp2.getUsername());
        testUser2.setNickname(userResp2.getNickname());
        testUser2.setEmail(userResp2.getEmail());
        testUser2.setPhone(userResp2.getPhone());
        testUser2.setStatus(userResp2.getStatus());
        testToken2 = JwtUtil.generateToken(testUser2.getId(), testUser2.getUsername());

        // 在用户上下文中创建测试项目
        testProjectId = executeWithUserContext(testUser.getId(), () -> {
            // 需要先创建项目，这里暂时使用固定ID
            // 实际项目中应该注入ProjectService来创建
            return 1L; // 假设项目ID为1，实际测试中应该先创建项目
        });

        // 在用户上下文中创建测试任务
        testTask = executeWithUserContext(testUser.getId(), () -> {
            TaskCreateReq taskCreateReq = new TaskCreateReq();
            taskCreateReq.setProjectId(1L);
            taskCreateReq.setTitle("测试任务");
            taskCreateReq.setDescription("这是一个测试任务");
            taskCreateReq.setType(TaskType.TASK);
            taskCreateReq.setPriority(TaskPriority.MEDIUM);
            taskCreateReq.setAssigneeId(testUser.getId());
            taskCreateReq.setStatus(TaskStatus.IN_PROGRESS);
            taskCreateReq.setEstimateHours(new BigDecimal("8.00"));

            Task createdTask = taskService.create(taskCreateReq);
            return createdTask;
        });
    }

    @Test
    void testGetTaskById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/" + testTask.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testTask.getId()))
                .andExpect(jsonPath("$.data.title").value("测试任务"));
    }

    @Test
    void testGetTasksByCondition() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/search")
                .header("Authorization", "Bearer " + testToken)
                .param("projectId", "1")
                .param("assigneeId", testUser.getId().toString())
                .param("status", "IN_PROGRESS")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.pageNum").value(1));
    }

    @Test
    void testCreateTask_Success() throws Exception {
        // Given
        TaskCreateReq req = new TaskCreateReq();
        req.setProjectId(1L);
        req.setTitle("新建测试任务");
        req.setDescription("通过API创建的任务");
        req.setType(TaskType.BUG);
        req.setPriority(TaskPriority.HIGH);
        req.setAssigneeId(testUser.getId());
        req.setStatus(TaskStatus.TODO);
        req.setEstimateHours(new BigDecimal("4.00"));

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("新建测试任务"));
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        // Given
        TaskUpdateReq req = new TaskUpdateReq();
        req.setId(testTask.getId());
        req.setProjectId(1L);
        req.setTitle("更新后的任务");
        req.setDescription("任务已更新");
        req.setType(TaskType.TASK);
        req.setPriority(TaskPriority.HIGH);
        req.setAssigneeId(testUser.getId());
        req.setStatus(TaskStatus.IN_PROGRESS);
        req.setEstimateHours(new BigDecimal("10.00"));

        // When & Then
        mockMvc.perform(put("/api/tasks")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("更新后的任务"));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tasks/" + testTask.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("任务删除成功"));
    }
}
