package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.UserStatus;
import com.gsms.gsms.dto.user.UserLoginReq;
import com.gsms.gsms.infra.utils.JwtUtil;
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
 * 用户控制器集成测试类
 * 继承BaseControllerTest，使用真实Service和数据库
 * 使用 @SpringBootTest + @AutoConfigureMockMvc，结合真实 UserService 和 JWT 认证
 * 如果要验证某个测试用例是否通过，可以查看控制台输出的日志。也可以使用 @Commit 注解，这样数据库就不会回滚。
 * 同时也要注意    @BeforeEach会在测试用例执行前，会先执行一次，所以可能会影响其他测试用例的执行结果。
 * 这时候可以用@TestMethodOrder(MethodOrderer.OrderAnnotation.class) + @Order(1) 注解，来指定测试用例的执行顺序。
 * 特别需要注意@TestMethodOrder(MethodOrderer.OrderAnnotation.class)要写到所有注解的最上面，否则无效。
 */
public class UserControllerTest extends BaseControllerTest {

    @Autowired
    private UserService userService;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setNickname("测试用户");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(UserStatus.DISABLED);

        // 通过真实 UserService 创建用户（依赖 DB）
        testUser = userService.createUser(user);

        // 使用 JwtUtil 生成真实可验证的 Token
        testToken = JwtUtil.generateToken(testUser.getId(), testUser.getUsername());
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/" + testUser.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        // Given - 使用一个不存在的用户ID
        Long nonExistId = testUser.getId() + 1000;

        // When & Then
        mockMvc.perform(get("/api/users/" + nonExistId)
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2002));
    }

    @Test
    void testGetAllUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].username").value("testuser"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pageNum").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Given - 使用不同用户名，避免唯一索引冲突
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password2");
        newUser.setNickname("新用户");
        newUser.setEmail("new@example.com");
        newUser.setPhone("13900139000");
        newUser.setStatus(UserStatus.DISABLED);

        // When & Then
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(newUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Given - 修改已有用户的昵称
        testUser.setNickname("更新后用户");

        // When & Then
        mockMvc.perform(put("/api/users")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(testUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.nickname").value("更新后用户"));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("用户删除成功"));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        UserLoginReq loginReq = new UserLoginReq();
        loginReq.setUsername("testuser");
        loginReq.setPassword("password");

        // When & Then - 调用真实登录接口，生成 JWT
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(loginReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    void testGetUsersByCondition() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/search?username=testuser")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].username").value("testuser"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pageNum").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void testGetUsersByCondition_WithPaging() throws Exception {
        // 创建更多测试用户
        for (int i = 1; i <= 15; i++) {
            User user = new User();
            user.setUsername("testuser" + i);
            user.setPassword("password");
            user.setNickname("测试用户" + i);
            user.setEmail("test" + i + "@example.com");
            user.setPhone("1380013800" + i);
            user.setStatus(UserStatus.DISABLED);
            userService.createUser(user);
        }
        
        // 测试分页查询 - 第一页，每页5条
        mockMvc.perform(get("/api/users/search?pageNum=1&pageSize=5")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.pageNum").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.total").value(16)); // 包括之前创建的1个用户
    }

    @Test
    void testLogin_Failure() throws Exception {
        // Given
        UserLoginReq loginReq = new UserLoginReq();
        loginReq.setUsername("testuser");
        loginReq.setPassword("wrongpassword");

        // When & Then - 密码错误，触发业务异常 PASSWORD_ERROR
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(loginReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2003));
    }
}