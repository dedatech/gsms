package com.gsms.gsms.controller;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectInfoResp;
import com.gsms.gsms.dto.project.ProjectUpdateReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.ProjectService;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;

import java.util.Date;
import java.time.LocalDate;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 项目控制器集成测试类
 */
public class ProjectControllerTest extends BaseControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    private User testUser;
    private String testToken;
    private ProjectInfoResp testProject;

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
        testUser.setNickname(userResp.getNickname());

        testToken = JwtUtil.generateTokenStatic(testUser.getId(), testUser.getUsername());

        // 创建测试项目
        ProjectCreateReq projectCreateReq = new ProjectCreateReq();
        projectCreateReq.setName("GSMS项目");
        projectCreateReq.setCode("GSMS");
        projectCreateReq.setDescription("工时管理系统");
        projectCreateReq.setManagerId(testUser.getId());
        projectCreateReq.setStatus(ProjectStatus.NOT_STARTED);
        projectCreateReq.setPlanStartDate(LocalDate.now());
        LocalDate endDate = LocalDate.now();
        projectCreateReq.setPlanEndDate(endDate);

        executeWithUserContext(testUser.getId(), () -> {
            testProject = projectService.create(projectCreateReq);
            return null;
        });
    }

    @Test
    void testGetProjectById_Success() throws Exception {
        mockMvc.perform(get("/api/projects/" + testProject.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testProject.getId().intValue()))
                .andExpect(jsonPath("$.data.name").value("GSMS项目"))
                .andExpect(jsonPath("$.data.code").value("GSMS"));
    }

    @Test
    void testGetProjectById_NotFound() throws Exception {
        Long nonExistId = testProject.getId() + 1000;

        mockMvc.perform(get("/api/projects/" + nonExistId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001)); // PROJECT_NOT_FOUND
    }

    @Test
    void testGetAllProjects_Success() throws Exception {
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("GSMS项目"));
    }

    @Test
    void testCreateProject_Success() throws Exception {
        ProjectCreateReq createReq = new ProjectCreateReq();
        createReq.setName("新项目");
        createReq.setCode("NEW_PROJ");
        createReq.setDescription("新项目描述");
        createReq.setManagerId(testUser.getId());
        createReq.setStatus(ProjectStatus.NOT_STARTED);
        createReq.setPlanStartDate(LocalDate.now());
        LocalDate endDate6 = LocalDate.now();
        createReq.setPlanEndDate(endDate6);

        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("新项目"))
                .andExpect(jsonPath("$.data.code").value("NEW_PROJ"));
    }

    @Test
    void testCreateProject_DuplicateCode() throws Exception {
        ProjectCreateReq createReq = new ProjectCreateReq();
        createReq.setName("重复项目");
        createReq.setCode("GSMS"); // 重复的项目编码
        createReq.setDescription("重复编码项目");
        createReq.setManagerId(testUser.getId());
        createReq.setStatus(ProjectStatus.NOT_STARTED);

        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3003)); // PROJECT_CODE_EXISTS
    }

    @Test
    void testUpdateProject_Success() throws Exception {
        ProjectUpdateReq updateReq = new ProjectUpdateReq();
        updateReq.setId(testProject.getId());
        updateReq.setName("GSMS系统项目");
        updateReq.setDescription("工时管理系统开发");

        mockMvc.perform(put("/api/projects")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(updateReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("GSMS系统项目"));
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        mockMvc.perform(delete("/api/projects/" + testProject.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("项目删除成功"));
    }

    @Test
    void testGetProjectsByCondition() throws Exception {
        mockMvc.perform(get("/api/projects?name=GSMS")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("GSMS项目"));
    }

    @Test
    void testGetProjectsByStatus() throws Exception {
        // TODO: Spring MVC默认无法将字符串"NOT_STARTED"转换为枚举
        // 需要添加自定义Converter或使用整数code
        // 临时使用整数code测试，确认查询逻辑正确
        mockMvc.perform(get("/api/projects?status=NOT_STARTED")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value(ProjectStatus.NOT_STARTED.name()));
    }

    @Test
    void testGetProjectsByStatus_NoFilter() throws Exception {
        // 测试不传status参数，应该能查到所有项目
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
