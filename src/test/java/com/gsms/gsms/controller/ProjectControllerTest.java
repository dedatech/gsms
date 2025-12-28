package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.ProjectStatus;
import com.gsms.gsms.domain.enums.UserStatus;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.ProjectMemberService;
import com.gsms.gsms.service.ProjectService;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

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

    @Autowired
    private ProjectMemberService projectMemberService;

    private Project testProject;
    private User testUser;
    private User testUser2;
    private String testToken;
    private String testToken2;

    @BeforeEach
    void setUp() throws Exception {
        // 创建真实测试用户1
        User user = new User();
        user.setUsername("projecttestuser");
        user.setPassword("password123");
        user.setNickname("项目测试用户");
        user.setEmail("projecttest@example.com");
        user.setPhone("13900139000");
        user.setStatus(UserStatus.NORMAL);
        testUser = userService.createUser(user);
        testToken = JwtUtil.generateToken(testUser.getId(), testUser.getUsername());
        
        // 创建真实测试用户2
        User user2 = new User();
        user2.setUsername("projecttestuser2");
        user2.setPassword("password123");
        user2.setNickname("项目测试用户2");
        user2.setEmail("projecttest2@example.com");
        user2.setPhone("13900139001");
        user2.setStatus(UserStatus.NORMAL);
        testUser2 = userService.createUser(user2);
        testToken2 = JwtUtil.generateToken(testUser2.getId(), testUser2.getUsername());
        
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
                .header("Authorization", "Bearer " + testToken)
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.pageNum").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }
    
    @Test
    void testGetProjectsByPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + testToken)
                .param("pageNum", "1")
                .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.pageNum").value(1))
                .andExpect(jsonPath("$.pageSize").value(1));
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
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").isNumber());
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

    @Test
    void testListProjectMembers_Success() throws Exception {
        // When & Then - 项目创建者可以查看成员列表
        mockMvc.perform(get("/api/projects/" + testProject.getId() + "/members")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].userId").value(testUser.getId()));
    }

    @Test
    void testListProjectMembers_Forbidden() throws Exception {
        // When & Then - 非项目成员不能查看成员列表
        mockMvc.perform(get("/api/projects/" + testProject.getId() + "/members")
                .header("Authorization", "Bearer " + testToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1403));
    }

    @Test
    void testAddProjectMembers_Success() throws Exception {
        // Given - 准备添加的用户ID列表
        List<Long> userIds = Arrays.asList(testUser2.getId());

        // When & Then
        mockMvc.perform(post("/api/projects/" + testProject.getId() + "/members")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userIds))
                .param("roleType", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("添加项目成员成功"));
    }

    @Test
    void testUpdateMemberRole_Success() throws Exception {
        // Given - 先添加成员
        executeWithUserContext(testUser.getId(), () -> {
            projectMemberService.addMembers(testProject.getId(), Arrays.asList(testUser2.getId()), 2);
            return null;
        });

        // When & Then - 更新成员角色
        mockMvc.perform(put("/api/projects/" + testProject.getId() + "/members/" + testUser2.getId())
                .header("Authorization", "Bearer " + testToken)
                .param("roleType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("更新项目成员角色成功"));
    }

    @Test
    void testRemoveMember_Success() throws Exception {
        // Given - 先添加成员
        executeWithUserContext(testUser.getId(), () -> {
            projectMemberService.addMembers(testProject.getId(), Arrays.asList(testUser2.getId()), 2);
            return null;
        });

        // When & Then - 移除成员
        mockMvc.perform(delete("/api/projects/" + testProject.getId() + "/members/" + testUser2.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("移除项目成员成功"));
    }
}