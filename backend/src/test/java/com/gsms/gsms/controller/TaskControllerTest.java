package com.gsms.gsms.controller;

import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.IterationStatus;
import com.gsms.gsms.model.enums.ProjectType;
import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.model.enums.TaskPriority;
import com.gsms.gsms.model.enums.TaskStatus;
import com.gsms.gsms.model.enums.TaskType;
import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationInfoResp;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectInfoResp;
import com.gsms.gsms.dto.task.TaskCreateReq;
import com.gsms.gsms.dto.task.TaskInfoResp;
import com.gsms.gsms.dto.task.TaskUpdateReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.IterationService;
import com.gsms.gsms.service.ProjectService;
import com.gsms.gsms.service.TaskService;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.util.Date;
import java.time.LocalDate;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 任务控制器集成测试类
 */
public class TaskControllerTest extends BaseControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IterationService iterationService;

    @Autowired
    private TaskService taskService;

    private User testUser;
    private String testToken;
    private ProjectInfoResp testProject;
    private IterationInfoResp testIteration;
    private Task testTask;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试用户
        UserCreateReq userCreateReq = new UserCreateReq();
        userCreateReq.setUsername("testuser");
        userCreateReq.setPassword("password");
        userCreateReq.setNickname("测试用户");
        userCreateReq.setEmail("test@example.com");
        userCreateReq.setPhone("13800138000");

        UserInfoResp userResp = userService.create(userCreateReq);
        testUser = new User();
        testUser.setId(userResp.getId());
        testUser.setUsername(userResp.getUsername());
        testUser.setPassword("password");

        testToken = JwtUtil.generateTokenStatic(testUser.getId(), testUser.getUsername());

        executeWithUserContext(testUser.getId(), () -> {
            // 创建测试项目（中大型项目，支持迭代）
            ProjectCreateReq projectCreateReq = new ProjectCreateReq();
            projectCreateReq.setName("GSMS项目");
            projectCreateReq.setCode("GSMS");
            projectCreateReq.setManagerId(testUser.getId());
            projectCreateReq.setStatus(ProjectStatus.IN_PROGRESS);
            projectCreateReq.setProjectType(ProjectType.LARGE_SCALE); // 中大型项目
            testProject = projectService.create(projectCreateReq);

            // 创建测试迭代
            IterationCreateReq iterationCreateReq = new IterationCreateReq();
            iterationCreateReq.setProjectId(testProject.getId());
            iterationCreateReq.setName("Sprint 1");
            iterationCreateReq.setStatus(IterationStatus.IN_PROGRESS);
            iterationCreateReq.setPlanStartDate(LocalDate.now());
            LocalDate planEndDate = LocalDate.now();
            iterationCreateReq.setPlanEndDate(planEndDate);
            testIteration = iterationService.create(iterationCreateReq);

            // 创建测试任务
            TaskCreateReq taskCreateReq = new TaskCreateReq();
            taskCreateReq.setProjectId(testProject.getId());
            taskCreateReq.setIterationId(testIteration.getId());
            taskCreateReq.setTitle("开发用户管理功能");
            taskCreateReq.setDescription("实现用户CRUD操作");
            taskCreateReq.setType(TaskType.TASK);
            taskCreateReq.setPriority(TaskPriority.HIGH);
            taskCreateReq.setAssigneeId(testUser.getId());
            taskCreateReq.setStatus(TaskStatus.TODO);
            taskCreateReq.setPlanStartDate(LocalDate.now());
            LocalDate taskPlanEndDate = LocalDate.now();
            taskCreateReq.setPlanEndDate(taskPlanEndDate);
            taskCreateReq.setEstimateHours(new java.math.BigDecimal("40.0"));

            testTask = taskService.create(taskCreateReq);
            return null;
        });
    }

    @Test
    void testGetTaskById_Success() throws Exception {
        mockMvc.perform(get("/api/tasks/" + testTask.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testTask.getId().intValue()))
                .andExpect(jsonPath("$.data.title").value("开发用户管理功能"));
    }

    @Test
    void testGetTaskById_NotFound() throws Exception {
        Long nonExistId = testTask.getId() + 1000;

        mockMvc.perform(get("/api/tasks/" + nonExistId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4001)); // TASK_NOT_FOUND
    }

    @Test
    void testGetTasksByProjectId_Success() throws Exception {
        mockMvc.perform(get("/api/tasks/project/" + testProject.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("开发用户管理功能"));
    }

    @Test
    void testCreateTask_Success() throws Exception {
        TaskCreateReq createReq = new TaskCreateReq();
        createReq.setProjectId(testProject.getId());
        createReq.setIterationId(testIteration.getId());
        createReq.setTitle("开发部门管理功能");
        createReq.setDescription("实现部门CRUD操作");
        createReq.setType(TaskType.TASK);
        createReq.setPriority(TaskPriority.MEDIUM);
        createReq.setAssigneeId(testUser.getId());
        createReq.setStatus(TaskStatus.TODO);
        createReq.setEstimateHours(new java.math.BigDecimal("32.0"));

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("开发部门管理功能"));
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        TaskUpdateReq updateReq = new TaskUpdateReq();
        updateReq.setId(testTask.getId());
        updateReq.setProjectId(testProject.getId()); // 必需字段
        updateReq.setTitle("开发用户管理功能（优化版）");
        updateReq.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(put("/api/tasks")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(updateReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("开发用户管理功能（优化版）"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));  // 期望字符串而非数字码
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + testTask.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("任务删除成功"));
    }

    @Test
    void testSearchTasks_Success() throws Exception {
        mockMvc.perform(get("/api/tasks/search")
                .param("projectId", testProject.getId().toString())
                .param("assigneeId", testUser.getId().toString())
                // 不传 status 参数，它是可选的
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("开发用户管理功能"));
    }

    @Test
    void testGetTasksByCondition() throws Exception {
        mockMvc.perform(get("/api/tasks/search")
                .param("projectId", testProject.getId().toString())
                .param("assigneeId", testUser.getId().toString())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].assigneeId").value(testUser.getId().intValue()));
    }
}
