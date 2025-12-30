package com.gsms.gsms.controller;

import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.model.enums.UserStatus;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.dto.project.ProjectInfoResp;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
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
        UserCreateReq userCreateReq = new UserCreateReq();
        userCreateReq.setUsername("projecttestuser");
        userCreateReq.setPassword("password123");
        userCreateReq.setNickname("项目测试用户");
        userCreateReq.setEmail("projecttest@example.com");
        userCreateReq.setPhone("13900139000");
        UserInfoResp userResp = userService.create(userCreateReq);

        // 构建testUser对象
        testUser = new User();
        testUser.setId(userResp.getId());
        testUser.setUsername(userResp.getUsername());
        testUser.setPassword("password123");
        testUser.setNickname(userResp.getNickname());
        testUser.setEmail(userResp.getEmail());
        testUser.setPhone(userResp.getPhone());
        testUser.setStatus(userResp.getStatus());
        testToken = JwtUtil.generateToken(testUser.getId(), testUser.getUsername());

        // 创建真实测试用户2
        UserCreateReq userCreateReq2 = new UserCreateReq();
        userCreateReq2.setUsername("projecttestuser2");
        userCreateReq2.setPassword("password123");
        userCreateReq2.setNickname("项目测试用户2");
        userCreateReq2.setEmail("projecttest2@example.com");
        userCreateReq2.setPhone("13900139001");
        UserInfoResp userResp2 = userService.create(userCreateReq2);

        // 构建testUser2对象
        testUser2 = new User();
        testUser2.setId(userResp2.getId());
        testUser2.setUsername(userResp2.getUsername());
        testUser2.setPassword("password123");
        testUser2.setNickname(userResp2.getNickname());
        testUser2.setEmail(userResp2.getEmail());
        testUser2.setPhone(userResp2.getPhone());
        testUser2.setStatus(userResp2.getStatus());
        testToken2 = JwtUtil.generateToken(testUser2.getId(), testUser2.getUsername());

        // 在用户上下文中创建项目
        testProject = executeWithUserContext(testUser.getId(), () -> {
            ProjectCreateReq projectCreateReq = new ProjectCreateReq();
            projectCreateReq.setName("测试项目");
            projectCreateReq.setCode("TEST-001");
            projectCreateReq.setDescription("这是一个测试项目");
            projectCreateReq.setManagerId(testUser.getId());
            projectCreateReq.setStatus(ProjectStatus.IN_PROGRESS);
            ProjectInfoResp projectResp = projectService.create(projectCreateReq);

            // 将ProjectInfoResp转换为Project实体
            Project project = new Project();
            project.setId(projectResp.getId());
            project.setName(projectResp.getName());
            project.setCode(projectResp.getCode());
            project.setDescription(projectResp.getDescription());
            project.setManagerId(projectResp.getManagerId());
            project.setStatus(projectResp.getStatus());
            return project;
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

        // When & Then - 权限检查会在项目存在性检查之前执行，返回权限错误
        mockMvc.perform(get("/api/projects/" + nonExistId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1403));
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
                .andExpect(jsonPath("$.pageNum").value(1));
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
        mockMvc.perform(get("/api/projects")
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