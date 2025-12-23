package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.domain.enums.ProjectStatus;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.infra.config.JwtInterceptor;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目控制器测试类
 */
@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    private Project testProject;
    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("测试项目");
        testProject.setCode("TEST-001");
        testProject.setDescription("这是一个测试项目");
        testProject.setManagerId(1L);
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setCreateUserId(1L);
        
        // 生成测试用的JWT Token
        testToken = "test.jwt.token";
        
        // Mock JWT拦截器，让所有请求通过
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void testGetProjectById_Success() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(testProject);

        // When & Then
        mockMvc.perform(get("/api/projects/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    void testGetProjectById_NotFound() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenThrow(new RuntimeException("项目不存在"));

        // When & Then
        mockMvc.perform(get("/api/projects/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testGetAllProjects() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getAllProjects()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试项目"));
    }

    @Test
    void testGetProjectsByCondition() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getProjectsByCondition("测试", ProjectStatus.IN_PROGRESS.getCode())).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects/search")
                .header("Authorization", "Bearer " + testToken)
                .param("name", "测试")
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试项目"));
    }

    @Test
    void testCreateProject_Success() throws Exception {
        // Given
        ProjectCreateReq req = new ProjectCreateReq();
        req.setName("测试项目");
        req.setCode("TEST-001");
        req.setDescription("这是一个测试项目");
        req.setManagerId(1L);
        req.setStatus(ProjectStatus.IN_PROGRESS);
        
        when(projectService.createProject(any(Project.class))).thenReturn(testProject);

        // When & Then
        try (MockedStatic<UserContext> mockedContext = mockStatic(UserContext.class)) {
            mockedContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("测试项目"));
        }
    }

    @Test
    void testUpdateProject_Success() throws Exception {
        // Given
        ProjectUpdateReq req = new ProjectUpdateReq();
        req.setId(1L);
        req.setName("测试项目");
        req.setCode("TEST-001");
        req.setDescription("这是一个测试项目");
        req.setManagerId(1L);
        req.setStatus(ProjectStatus.IN_PROGRESS);
        
        when(projectService.updateProject(any(Project.class))).thenReturn(testProject);

        // When & Then
        try (MockedStatic<UserContext> mockedContext = mockStatic(UserContext.class)) {
            mockedContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            
            mockMvc.perform(put("/api/projects")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        // Given
        doNothing().when(projectService).deleteProject(1L);

        // When & Then
        mockMvc.perform(delete("/api/projects/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("项目删除成功"));
    }
}