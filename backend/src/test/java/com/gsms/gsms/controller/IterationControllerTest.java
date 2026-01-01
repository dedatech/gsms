package com.gsms.gsms.controller;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.IterationStatus;
import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationInfoResp;
import com.gsms.gsms.dto.iteration.IterationUpdateReq;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectInfoResp;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.IterationService;
import com.gsms.gsms.service.ProjectService;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.util.Date;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 迭代控制器集成测试类
 */
public class IterationControllerTest extends BaseControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IterationService iterationService;

    private User testUser;
    private String testToken;
    private ProjectInfoResp testProject;
    private IterationInfoResp testIteration;

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

        testToken = JwtUtil.generateToken(testUser.getId(), testUser.getUsername());

        // 创建测试项目
        ProjectCreateReq projectCreateReq = new ProjectCreateReq();
        projectCreateReq.setName("GSMS项目");
        projectCreateReq.setCode("GSMS");
        projectCreateReq.setManagerId(testUser.getId());
        projectCreateReq.setStatus(ProjectStatus.NOT_STARTED);

        executeWithUserContext(testUser.getId(), () -> {
            testProject = projectService.create(projectCreateReq);

            // 创建测试迭代
            IterationCreateReq iterationCreateReq = new IterationCreateReq();
            iterationCreateReq.setProjectId(testProject.getId());
            iterationCreateReq.setName("Sprint 1");
            iterationCreateReq.setDescription("第一个迭代");
            iterationCreateReq.setStatus(IterationStatus.NOT_STARTED);
            iterationCreateReq.setPlanStartDate(new Date());
            Date endDate2 = new Date();
            endDate2.setTime(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000);
            iterationCreateReq.setPlanEndDate(endDate2);

            testIteration = iterationService.create(iterationCreateReq);
            return null;
        });
    }

    @Test
    void testGetIterationById_Success() throws Exception {
        mockMvc.perform(get("/api/iterations/" + testIteration.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testIteration.getId().intValue()))
                .andExpect(jsonPath("$.data.name").value("Sprint 1"));
    }

    @Test
    void testGetIterationById_NotFound() throws Exception {
        Long nonExistId = testIteration.getId() + 1000;

        mockMvc.perform(get("/api/iterations/" + nonExistId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001)); // ITERATION_NOT_FOUND
    }

    @Test
    void testGetIterationsByProjectId_Success() throws Exception {
        mockMvc.perform(get("/api/iterations/project/" + testProject.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Sprint 1"));
    }

    @Test
    void testCreateIteration_Success() throws Exception {
        IterationCreateReq createReq = new IterationCreateReq();
        createReq.setProjectId(testProject.getId());
        createReq.setName("Sprint 2");
        createReq.setDescription("第二个迭代");
        createReq.setStatus(IterationStatus.IN_PROGRESS);
        createReq.setPlanStartDate(new Date());
        Date endDate2w = new Date();
        endDate2w.setTime(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000);
        createReq.setPlanEndDate(endDate2w);

        mockMvc.perform(post("/api/iterations")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Sprint 2"));
    }

    @Test
    void testUpdateIteration_Success() throws Exception {
        IterationUpdateReq updateReq = new IterationUpdateReq();
        updateReq.setId(testIteration.getId());
        updateReq.setName("Sprint 1 - 开发阶段");
        updateReq.setStatus(IterationStatus.IN_PROGRESS);

        mockMvc.perform(put("/api/iterations")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(updateReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Sprint 1 - 开发阶段"));
    }

    @Test
    void testDeleteIteration_Success() throws Exception {
        mockMvc.perform(delete("/api/iterations/" + testIteration.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("迭代删除成功"));
    }

    @Test
    void testGetIterationsByCondition() throws Exception {
        mockMvc.perform(get("/api/iterations?projectId=" + testProject.getId() + "&status=1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value(IterationStatus.NOT_STARTED.getCode()));
    }
}
