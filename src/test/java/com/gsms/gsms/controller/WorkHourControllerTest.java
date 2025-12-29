package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.Task;
import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.entity.WorkHour;
import com.gsms.gsms.domain.enums.TaskPriority;
import com.gsms.gsms.domain.enums.TaskStatus;
import com.gsms.gsms.domain.enums.TaskType;
import com.gsms.gsms.domain.enums.WorkHourStatus;
import com.gsms.gsms.domain.enums.ProjectStatus;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.workhour.WorkHourCreateReq;
import com.gsms.gsms.dto.workhour.WorkHourUpdateReq;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectInfoResp;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.TaskService;
import com.gsms.gsms.service.UserService;
import com.gsms.gsms.service.WorkHourService;
import com.gsms.gsms.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 工时记录控制器集成测试类
 * 继承BaseControllerTest，使用真实Service和数据库
 */
public class WorkHourControllerTest extends BaseControllerTest {

    @Autowired
    private WorkHourService workHourService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    private WorkHour testWorkHour;
    private User testUser;
    private String testToken;
    private Task testTask;
    private Long testProjectId;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试用户
        UserCreateReq userCreateReq = new UserCreateReq();
        userCreateReq.setUsername("workhourtestuser");
        userCreateReq.setPassword("password123");
        userCreateReq.setNickname("工时测试用户");
        userCreateReq.setEmail("workhourtest@example.com");
        userCreateReq.setPhone("13800138200");
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

        // 在用户上下文中创建测试项目
        testProjectId = executeWithUserContext(testUser.getId(), () -> {
            ProjectCreateReq projectCreateReq = new ProjectCreateReq();
            projectCreateReq.setName("工时测试项目");
            projectCreateReq.setCode("WH-TEST");
            projectCreateReq.setDescription("用于工时测试的项目");
            projectCreateReq.setManagerId(testUser.getId());
            projectCreateReq.setStatus(ProjectStatus.IN_PROGRESS);
            ProjectInfoResp projectResp = projectService.create(projectCreateReq);
            return projectResp.getId();
        });

        // 在用户上下文中创建测试任务
        testTask = executeWithUserContext(testUser.getId(), () -> {
            TaskCreateReq taskCreateReq = new TaskCreateReq();
            taskCreateReq.setProjectId(testProjectId);
            taskCreateReq.setTitle("工时测试任务");
            taskCreateReq.setDescription("用于工时测试的任务");
            taskCreateReq.setType(TaskType.TASK);
            taskCreateReq.setPriority(TaskPriority.MEDIUM);
            taskCreateReq.setAssigneeId(testUser.getId());
            taskCreateReq.setStatus(TaskStatus.IN_PROGRESS);
            taskCreateReq.setEstimateHours(new BigDecimal("8.00"));

            return taskService.create(taskCreateReq);
        });

        // 在用户上下文中创建测试工时记录
        testWorkHour = executeWithUserContext(testUser.getId(), () -> {
            WorkHour workHour = new WorkHour();
            workHour.setUserId(testUser.getId());
            workHour.setProjectId(testProjectId);
            workHour.setTaskId(testTask.getId());
            workHour.setWorkDate(new Date());
            workHour.setHours(new BigDecimal("8.00"));
            workHour.setContent("开发任务");
            workHour.setStatus(WorkHourStatus.SAVED);
            workHour.setCreateUserId(testUser.getId());
            workHour.setUpdateUserId(testUser.getId());
            return workHourService.createWorkHour(workHour);
        });
    }

    @Test
    void testGetWorkHourById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/work-hours/" + testWorkHour.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testWorkHour.getId()));
    }

    @Test
    void testGetWorkHoursByUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/work-hours/user/" + testUser.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetWorkHoursByProjectId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/work-hours/project/" + testProjectId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testCreateWorkHour_Success() throws Exception {
        // Given
        WorkHourCreateReq req = new WorkHourCreateReq();
        req.setProjectId(testProjectId);
        req.setTaskId(testTask.getId());
        req.setWorkDate(new Date());
        req.setHours(new BigDecimal("4.00"));
        req.setContent("测试工时记录");

        // When & Then
        mockMvc.perform(post("/api/work-hours")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("测试工时记录"));
    }

    @Test
    void testUpdateWorkHour_Success() throws Exception {
        // Given
        WorkHourUpdateReq req = new WorkHourUpdateReq();
        req.setId(testWorkHour.getId());
        req.setProjectId(testProjectId);
        req.setTaskId(testTask.getId());
        req.setWorkDate(new Date());
        req.setHours(new BigDecimal("6.00"));
        req.setContent("更新后的工时记录");
        req.setStatus(WorkHourStatus.SUBMITTED);

        // When & Then
        mockMvc.perform(put("/api/work-hours")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("更新后的工时记录"));
    }

    @Test
    void testDeleteWorkHour_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/work-hours/" + testWorkHour.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
