package com.gsms.gsms.controller;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.UserStatus;
import com.gsms.gsms.dto.user.UserLoginReq;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.dto.user.UserUpdateReq;
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
        // 创建UserCreateReq
        UserCreateReq createReq = new UserCreateReq();
        createReq.setUsername("testuser");
        createReq.setPassword("password");
        createReq.setNickname("测试用户");
        createReq.setEmail("test@example.com");
        createReq.setPhone("13800138000");

        // 通过真实 UserService 创建用户（依赖 DB）
        UserInfoResp userResp = userService.create(createReq);

        // 构建testUser对象用于测试
        testUser = new User();
        testUser.setId(userResp.getId());
        testUser.setUsername(userResp.getUsername());
        testUser.setPassword("password");
        testUser.setNickname(userResp.getNickname());
        testUser.setEmail(userResp.getEmail());
        testUser.setPhone(userResp.getPhone());
        testUser.setStatus(userResp.getStatus());

        // 使用 JwtUtil 生成真实可验证的 Token
        testToken = JwtUtil.generateTokenStatic(testUser.getId(), testUser.getUsername());
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
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.pageNum").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Given - 使用不同用户名，避免唯一索引冲突
        UserCreateReq createReq = new UserCreateReq();
        createReq.setUsername("newuser");
        createReq.setPassword("password2");
        createReq.setNickname("新用户");
        createReq.setEmail("new@example.com");
        createReq.setPhone("13900139000");

        // When & Then
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReq))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Given - 修改已有用户的昵称
        UserUpdateReq updateReq = new UserUpdateReq();
        updateReq.setId(testUser.getId());
        updateReq.setNickname("更新后用户");

        // When & Then
        mockMvc.perform(put("/api/users")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(updateReq))))
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
                .andExpect(jsonPath("$.data").value("删除成功"));
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
        mockMvc.perform(get("/api/users?username=testuser")
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
            UserCreateReq createReq = new UserCreateReq();
            createReq.setUsername("testuser" + i);
            createReq.setPassword("password");
            createReq.setNickname("测试用户" + i);
            createReq.setEmail("test" + i + "@example.com");
            createReq.setPhone("1380013800" + String.format("%02d", i % 100));
            userService.create(createReq);
        }
        
        // 测试分页查询 - 第一页，每页5条
        mockMvc.perform(get("/api/users?pageNum=1&pageSize=5")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.pageNum").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.total").value(17)); // 包括admin、testuser和新建的15个用户
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

    @Test
    void testBatchCreateUsers_Success() throws Exception {
        // Given - 准备批量创建的用户列表
        java.util.List<UserCreateReq> createReqList = new java.util.ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            UserCreateReq req = new UserCreateReq();
            req.setUsername("batchuser" + i);
            req.setPassword("password123");
            req.setNickname("批量用户" + i);
            req.setEmail("batchuser" + i + "@example.com");
            req.setPhone("1390013900" + String.format("%02d", i));
            createReqList.add(req);
        }

        // When & Then
        mockMvc.perform(post("/api/users/batch")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReqList))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量创建完成: 成功3个"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].username").value("batchuser1"))
                .andExpect(jsonPath("$.data[1].username").value("batchuser2"))
                .andExpect(jsonPath("$.data[2].username").value("batchuser3"));
    }

    @Test
    void testBatchCreateUsers_PartialSuccess() throws Exception {
        // Given - 准备批量创建的用户列表，其中一个用户名已存在
        java.util.List<UserCreateReq> createReqList = new java.util.ArrayList<>();
        
        // 第一个用户 - 正常
        UserCreateReq req1 = new UserCreateReq();
        req1.setUsername("batchuser10");
        req1.setPassword("password123");
        req1.setNickname("批量用户10");
        req1.setEmail("batchuser10@example.com");
        req1.setPhone("13900139010");
        createReqList.add(req1);
        
        // 第二个用户 - 用户名与testuser冲突
        UserCreateReq req2 = new UserCreateReq();
        req2.setUsername("testuser");  // 已存在的用户名
        req2.setPassword("password123");
        req2.setNickname("重复用户");
        req2.setEmail("duplicate@example.com");
        req2.setPhone("13900139011");
        createReqList.add(req2);
        
        // 第三个用户 - 正常
        UserCreateReq req3 = new UserCreateReq();
        req3.setUsername("batchuser11");
        req3.setPassword("password123");
        req3.setNickname("批量用户11");
        req3.setEmail("batchuser11@example.com");
        req3.setPhone("13900139012");
        createReqList.add(req3);

        // When & Then - 应该成功创建2个，1个失败
        mockMvc.perform(post("/api/users/batch")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReqList))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量创建完成: 成功2个, 失败1个"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].username").value("batchuser10"))
                .andExpect(jsonPath("$.data[1].username").value("batchuser11"));
    }

    @Test
    void testBatchCreateUsers_EmptyList() throws Exception {
        // Given - 空列表
        java.util.List<UserCreateReq> createReqList = new java.util.ArrayList<>();

        // When & Then - 应该返回错误
        mockMvc.perform(post("/api/users/batch")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReqList))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001));  // USER_CREATE_FAILED
    }

    @Test
    void testBatchCreateUsers_LargeBatch() throws Exception {
        // Given - 批量创建10个用户
        java.util.List<UserCreateReq> createReqList = new java.util.ArrayList<>();
        
        for (int i = 20; i < 30; i++) {
            UserCreateReq req = new UserCreateReq();
            req.setUsername("batchuser" + i);
            req.setPassword("password123");
            req.setNickname("批量用户" + i);
            req.setEmail("batchuser" + i + "@example.com");
            req.setPhone("1390013900" + String.format("%02d", i));
            createReqList.add(req);
        }

        // When & Then
        mockMvc.perform(post("/api/users/batch")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReqList))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量创建完成: 成功10个"))
                .andExpect(jsonPath("$.data.length()").value(10));
    }

    @Test
    void testBatchCreateUsers_ValidationFailure() throws Exception {
        // Given - 准备一个验证失败的用户（用户名太短）
        java.util.List<UserCreateReq> createReqList = new java.util.ArrayList<>();
        
        UserCreateReq req = new UserCreateReq();
        req.setUsername("ab");  // 用户名太短，不满足3个字符
        req.setPassword("password123");
        req.setNickname("测试用户");
        req.setEmail("test@example.com");
        req.setPhone("13900139000");
        createReqList.add(req);

        // When & Then - 应该返回验证错误
        mockMvc.perform(post("/api/users/batch")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(createReqList))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));  // 验证失败
    }

    // ==================== 个人信息管理功能测试 ====================

    @Test
    void testGetCurrentUserInfo_Success() throws Exception {
        // When & Then - 获取当前登录用户信息
        mockMvc.perform(get("/api/users/current")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"));
    }

    @Test
    void testGetCurrentUserInfo_Unauthorized() throws Exception {
        // When & Then - 未登录访问，应该返回 401
        mockMvc.perform(get("/api/users/current"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCurrentUserInfo_Success() throws Exception {
        // Given - 准备更新数据
        String updateJson = "{\"nickname\":\"新昵称\",\"email\":\"newemail@example.com\",\"phone\":\"13900139000\"}";

        // When & Then - 更新当前用户信息
        mockMvc.perform(put("/api/users/current")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证更新是否成功 - 重新查询用户信息
        mockMvc.perform(get("/api/users/current")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.data.phone").value("13900139000"));
    }

    @Test
    void testUpdateCurrentUserInfo_UpdateNicknameOnly() throws Exception {
        // Given - 只更新昵称
        String updateJson = "{\"nickname\":\"只改昵称\"}";

        // When & Then
        mockMvc.perform(put("/api/users/current")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testUpdateCurrentUserInfo_ValidationFailed_EmailFormat() throws Exception {
        // Given - 邮箱格式错误
        String updateJson = "{\"email\":\"invalid-email\"}";

        // When & Then - 应该返回验证错误
        mockMvc.perform(put("/api/users/current")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));  // 参数校验失败
    }

    @Test
    void testUpdateCurrentUserInfo_ValidationFailed_PhoneFormat() throws Exception {
        // Given - 手机号格式错误
        String updateJson = "{\"phone\":\"12345\"}";

        // When & Then - 应该返回验证错误
        mockMvc.perform(put("/api/users/current")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));  // 参数校验失败
    }

    @Test
    void testUpdateCurrentUserInfo_ValidationFailed_NicknameTooShort() throws Exception {
        // Given - 昵称太短
        String updateJson = "{\"nickname\":\"a\"}";

        // When & Then - 应该返回验证错误
        mockMvc.perform(put("/api/users/current")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));  // 参数校验失败
    }

    @Test
    void testChangeCurrentPassword_Success() throws Exception {
        // Given - 准备修改密码数据
        String passwordJson = "{\"oldPassword\":\"password\",\"newPassword\":\"newPassword123\"}";

        // When & Then - 修改密码成功
        mockMvc.perform(put("/api/users/current/password")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 注意：修改密码后需要重新登录获取新 Token
        // 这里我们只测试密码修改接口本身是否成功
    }

    @Test
    void testChangeCurrentPassword_WrongOldPassword() throws Exception {
        // Given - 旧密码错误
        String passwordJson = "{\"oldPassword\":\"wrongPassword\",\"newPassword\":\"newPassword123\"}";

        // When & Then - 应该返回密码错误
        mockMvc.perform(put("/api/users/current/password")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2003));  // 旧密码错误
    }

    @Test
    void testChangeCurrentPassword_ValidationFailed_NewPasswordTooShort() throws Exception {
        // Given - 新密码太短
        String passwordJson = "{\"oldPassword\":\"password\",\"newPassword\":\"12345\"}";

        // When & Then - 应该返回验证错误
        mockMvc.perform(put("/api/users/current/password")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));  // 参数校验失败
    }

    @Test
    void testChangeCurrentPassword_ValidationFailed_MissingOldPassword() throws Exception {
        // Given - 缺少旧密码
        String passwordJson = "{\"newPassword\":\"newPassword123\"}";

        // When & Then - 应该返回验证错误
        mockMvc.perform(put("/api/users/current/password")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));  // 参数校验失败
    }

    @Test
    void testChangeCurrentPassword_Unauthorized() throws Exception {
        // Given - 准备修改密码数据
        String passwordJson = "{\"oldPassword\":\"password\",\"newPassword\":\"newPassword123\"}";

        // When & Then - 未登录访问，应该返回 401
        mockMvc.perform(put("/api/users/current/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordJson))
                .andExpect(status().isUnauthorized());
    }
}