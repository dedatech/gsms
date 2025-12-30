package com.gsms.gsms.controller;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.dto.department.DepartmentCreateReq;
import com.gsms.gsms.dto.department.DepartmentInfoResp;
import com.gsms.gsms.dto.department.DepartmentUpdateReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.service.DepartmentService;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 部门控制器集成测试类
 */
public class DepartmentControllerTest extends BaseControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    private User testUser;
    private String testToken;
    private DepartmentInfoResp testDepartment;

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

        testToken = JwtUtil.generateToken(testUser.getId(), testUser.getUsername());

        // 创建测试部门
        DepartmentCreateReq deptCreateReq = new DepartmentCreateReq();
        deptCreateReq.setName("技术部");
        deptCreateReq.setParentId(0L);
        deptCreateReq.setLevel(1);
        deptCreateReq.setSort(1);
        deptCreateReq.setRemark("技术研发部门");

        executeWithUserContext(testUser.getId(), () -> {
            testDepartment = departmentService.create(deptCreateReq);
            return null;
        });
    }

    @Test
    void testGetDepartmentById_Success() throws Exception {
        mockMvc.perform(get("/api/departments/" + testDepartment.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testDepartment.getId().intValue()))
                .andExpect(jsonPath("$.data.name").value("技术部"));
    }

    @Test
    void testGetDepartmentById_NotFound() throws Exception {
        Long nonExistId = testDepartment.getId() + 1000;

        mockMvc.perform(get("/api/departments/" + nonExistId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001)); // DEPARTMENT_NOT_FOUND
    }

    @Test
    void testGetAllDepartments() throws Exception {
        mockMvc.perform(get("/api/departments")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("技术部"));
    }

    @Test
    void testCreateDepartment_Success() throws Exception {
        DepartmentCreateReq createReq = new DepartmentCreateReq();
        createReq.setName("产品部");
        createReq.setParentId(0L);
        createReq.setLevel(1);
        createReq.setSort(2);
        createReq.setRemark("产品设计部门");

        mockMvc.perform(post("/api/departments")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("产品部"));
    }

    @Test
    void testCreateSubDepartment_Success() throws Exception {
        DepartmentCreateReq createReq = new DepartmentCreateReq();
        createReq.setName("前端开发组");
        createReq.setParentId(testDepartment.getId());
        createReq.setLevel(2);
        createReq.setSort(1);
        createReq.setRemark("前端开发小组");

        mockMvc.perform(post("/api/departments")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("前端开发组"))
                .andExpect(jsonPath("$.data.parentId").value(testDepartment.getId().intValue()));
    }

    @Test
    void testUpdateDepartment_Success() throws Exception {
        DepartmentUpdateReq updateReq = new DepartmentUpdateReq();
        updateReq.setId(testDepartment.getId());
        updateReq.setName("技术研发部");
        updateReq.setRemark("负责公司技术研发工作");

        mockMvc.perform(put("/api/departments")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(updateReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("技术研发部"));
    }

    @Test
    void testDeleteDepartment_Success() throws Exception {
        mockMvc.perform(delete("/api/departments/" + testDepartment.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("删除成功"));
    }

    @Test
    void testGetDepartmentsByCondition() throws Exception {
        mockMvc.perform(get("/api/departments?name=技术")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("技术部"));
    }
}
