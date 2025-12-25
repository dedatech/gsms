package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.domain.entity.ProjectMember;
import com.gsms.gsms.domain.enums.ProjectStatus;
import com.gsms.gsms.domain.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.infra.config.JwtInterceptor;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.ProjectMemberService;
import com.gsms.gsms.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目控制器单元测试类
 * 使用 @SpringBootTest + MockBean 模式，验证 Controller 层业务逻辑
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectMemberService projectMemberService;

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

    /**
     * 测试项目不存在的异常处理
     * 测试目的：验证 Controller 层能正确捕获 Service 层抛出的 BusinessException，
     *          并通过 GlobalExceptionHandler 转换为正确的错误响应结构
     * 验证点：
     *   1. HTTP 状态码为 200（业务异常不应该返回 500）
     *   2. 响应 code = 3001（ProjectErrorCode.PROJECT_NOT_FOUND）
     *   3. 响应 message = "项目不存在"
     * 注意：这是单元测试，通过 Mock 模拟异常场景，不依赖真实数据库
     */
    @Test
    void testGetProjectById_NotFound() throws Exception {
        // Given - Mock Service 抛出业务异常
        when(projectService.getProjectById(999L)).thenThrow(
            new BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)
        );

        // When & Then - 验证异常处理机制
        mockMvc.perform(get("/api/projects/999")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001))
                .andExpect(jsonPath("$.message").value("项目不存在"));
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

    @Test
    void testListProjectMembers_Success() throws Exception {
        // Given
        ProjectMember member = new ProjectMember();
        member.setId(1L);
        member.setProjectId(1L);
        member.setUserId(2L);
        member.setRoleType(2);

        List<ProjectMember> members = Arrays.asList(member);
        when(projectMemberService.listMembersByProjectId(1L)).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/api/projects/1/members")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()"
                ).value(1))
                .andExpect(jsonPath("$.data[0].userId").value(2));
    }

    @Test
    void testAddProjectMembers_Success() throws Exception {
        // Given
        List<Long> userIds = Arrays.asList(2L, 3L);
        doNothing().when(projectMemberService).addMembers(eq(1L), anyList(), eq(2));

        // When & Then
        mockMvc.perform(post("/api/projects/1/members")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("添加项目成员成功"));
    }

    @Test
    void testUpdateProjectMemberRole_Success() throws Exception {
        // Given
        doNothing().when(projectMemberService).updateMemberRole(1L, 2L, 3);

        // When & Then
        mockMvc.perform(put("/api/projects/1/members/2")
                .header("Authorization", "Bearer " + testToken)
                .param("roleType", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("更新项目成员角色成功"));
    }

    @Test
    void testRemoveProjectMember_Success() throws Exception {
        // Given
        doNothing().when(projectMemberService).removeMember(1L, 2L);

        // When & Then
        mockMvc.perform(delete("/api/projects/1/members/2")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("移除项目成员成功"));
    }
}