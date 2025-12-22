package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    @Autowired
    private ObjectMapper objectMapper;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("测试项目");
        testProject.setCode("TEST-001");
        testProject.setDescription("这是一个测试项目");
        testProject.setManagerId(1L);
        testProject.setStatus(1);
        testProject.setCreateUserId(1L);
    }

    @Test
    void testGetProjectById_Success() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(testProject);

        // When & Then
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    void testGetProjectById_NotFound() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }

    @Test
    void testGetAllProjects() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getAllProjects()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试项目"));
    }

    @Test
    void testGetProjectsByCondition() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getProjectsByCondition("测试", 1)).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects/search")
                .param("name", "测试")
                .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试项目"));
    }

    @Test
    void testCreateProject_Success() throws Exception {
        // Given
        when(projectService.createProject(any(Project.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("项目创建成功"));
    }

    @Test
    void testUpdateProject_Success() throws Exception {
        // Given
        when(projectService.updateProject(any(Project.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("项目更新成功"));
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        // Given
        when(projectService.deleteProject(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("项目删除成功"));
    }
}