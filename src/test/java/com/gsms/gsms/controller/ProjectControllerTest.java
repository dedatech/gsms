package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.ProjectStatus;
import com.gsms.gsms.domain.enums.UserStatus;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.ProjectService;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目控制器集成测试类
 * 继承BaseControllerTest，使用真实Service和数据库
 */
public class ProjectControllerTest extends BaseControllerTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    private Project testProject;
    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        // 创建真实测试用户
        User user = new User();
        user.setUsername("projecttestuser");
        user.setPassword("password123");
        user.setNickname("项目测试用户");
        user.setEmail("projecttest@example.com");
        user.setPhone("13900139000");
        user.setStatus(UserStatus.NORMAL);
        testUser = userService.createUser(user);
        
        // 生成真实JWT Token
        testToken = JwtUtil.generateToken(testUser.getId(), testUser.getUsername());
        
        // 在用户上下文中创建项目
        testProject = executeWithUserContext(testUser.getId(), () -> {
            Project project = new Project();
            project.setName("测试项目");
            project.setCode("TEST-001");
            project.setDescription("这是一个测试项目");
            project.setManagerId(testUser.getId());
            project.setStatus(ProjectStatus.IN_PROGRESS);
            return projectService.createProject(project);
        });
    }

    @Test
    void testGetProjectById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/projects/" + testProject.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testProject.getId()))
                .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    void testGetProjectById_NotFound() throws Exception {
        // Given - 使用不存在的项目ID
        Long nonExistId = testProject.getId() + 9999;

        // When & Then
        mockMvc.perform(get("/api/projects/" + nonExistId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }

    @Test
    void testGetAllProjects() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetProjectsByCondition() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/projects/search")
                .header("Authorization", "Bearer " + testToken)
                .param("name", "测试")
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testCreateProject_Success() throws Exception {
        // Given
        ProjectCreateReq req = new ProjectCreateReq();
        req.setName("新建测试项目");
        req.setCode("TEST-002");
        req.setDescription("通过API创建的项目");
        req.setManagerId(testUser.getId());
        req.setStatus(ProjectStatus.IN_PROGRESS);

        // When & Then
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("新建测试项目"));
    }

    @Test
    void testUpdateProject_Success() throws Exception {
        // Given
        ProjectUpdateReq req = new ProjectUpdateReq();
        req.setId(testProject.getId());
        req.setName("更新后的项目");
        req.setCode("TEST-001-UPDATED");
        req.setDescription("项目已更新");
        req.setManagerId(testUser.getId());
        req.setStatus(ProjectStatus.ARCHIVED);

        // When & Then
        mockMvc.perform(put("/api/projects")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("更新后的项目"));
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/projects/" + testProject.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("项目删除成功"));
    }
}