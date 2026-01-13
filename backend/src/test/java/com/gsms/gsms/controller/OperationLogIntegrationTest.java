package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.dto.user.UserCreateReq;
import com.gsms.gsms.dto.user.UserUpdateReq;
import com.gsms.gsms.infra.utils.JwtUtil;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.repository.OperationLogMapper;
import com.gsms.gsms.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 操作日志变更追踪集成测试
 * 验证 CREATE/UPDATE/DELETE 操作的 old_value 和 new_value 记录
 */
@Transactional
public class OperationLogIntegrationTest extends BaseControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String testToken;
    private Long createdUserId;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试用户（使用时间戳避免重复）
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueUsername = "logtestuser_" + timestamp.substring(timestamp.length() - 6);

        UserCreateReq createReq = new UserCreateReq();
        createReq.setUsername(uniqueUsername);
        createReq.setPassword("Test123");
        createReq.setNickname("日志测试用户");
        createReq.setEmail("logtest" + timestamp + "@example.com");

        com.gsms.gsms.dto.user.UserInfoResp userResp = userService.create(createReq);
        createdUserId = userResp.getId();

        testUser = new User();
        testUser.setId(userResp.getId());
        testUser.setUsername(userResp.getUsername());

        testToken = JwtUtil.generateTokenStatic(testUser.getId(), testUser.getUsername());
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        try {
            if (createdUserId != null) {
                userService.delete(createdUserId);
            }
        } catch (Exception e) {
            // 忽略删除失败
        }
    }

    @Test
    @DisplayName("测试 CREATE 用户的操作日志 - old_value 为 null，new_value 包含完整 JSON")
    void testCreateUser_OperationLog() throws Exception {
        // When - 创建新用户
        UserCreateReq createReq = new UserCreateReq();
        createReq.setUsername("newuser001");
        createReq.setPassword("Test123");
        createReq.setNickname("新用户");
        createReq.setEmail("newuser@example.com");

        String createJson = objectMapper.writeValueAsString(createReq);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser001"));

        // Then - 查询操作日志
        java.util.List<com.gsms.gsms.model.entity.OperationLog> logs = operationLogMapper.findByCondition(
                null,  // username
                null,  // module
                null,  // operationType
                null,  // status
                null,  // startTime
                null   // endTime
        );

        // 找到最新的 CREATE 操作日志
        com.gsms.gsms.model.entity.OperationLog createLog = logs.stream()
                .filter(log -> log.getOperationType().name().equals("CREATE"))
                .filter(log -> log.getBusinessType() != null && log.getBusinessType().equals("USER"))
                .filter(log -> log.getOperationContent() != null && log.getOperationContent().contains("newuser001"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到 CREATE 操作日志。提示：测试数据库可能未执行 Flyway 迁移 V20260113"));

        // 验证 business_type 和 business_id
        assert createLog.getBusinessType().equals("USER") : "business_type 应该是 USER";
        assert createLog.getBusinessId() != null : "business_id 不应该为 null";

        // 验证 old_value 为 null（CREATE 操作没有旧值）
        assert createLog.getOldValue() == null : "CREATE 操作的 old_value 应该为 null";

        // 验证 new_value 包含完整的 JSON 数据
        assert createLog.getNewValue() != null : "CREATE 操作的 new_value 不应该为 null";
        com.fasterxml.jackson.databind.JsonNode newNode = objectMapper.readTree(createLog.getNewValue());
        assert newNode.has("username") : "new_value JSON 应该包含 username 字段";
        assert newNode.get("username").asText().equals("newuser001") : "username 应该是 newuser001";
        assert newNode.has("nickname") : "new_value JSON 应该包含 nickname 字段";
        assert newNode.has("email") : "new_value JSON 应该包含 email 字段";

        System.out.println("✅ CREATE 操作日志验证通过");
        System.out.println("   - business_type: " + createLog.getBusinessType());
        System.out.println("   - business_id: " + createLog.getBusinessId());
        System.out.println("   - old_value: " + createLog.getOldValue());
        System.out.println("   - new_value: " + createLog.getNewValue());
    }

    @Test
    @DisplayName("测试 UPDATE 用户的操作日志 - old_value 和 new_value 都包含 JSON")
    void testUpdateUser_OperationLog() throws Exception {
        // Given - 先更新用户
        UserUpdateReq updateReq = new UserUpdateReq();
        updateReq.setId(createdUserId);
        updateReq.setNickname("更新后的昵称");
        updateReq.setEmail("updated@example.com");

        String updateJson = objectMapper.writeValueAsString(updateReq);

        // When - 执行更新
        mockMvc.perform(put("/api/users/" + createdUserId)
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("更新后的昵称"));

        // Then - 查询操作日志
        java.util.List<com.gsms.gsms.model.entity.OperationLog> logs = operationLogMapper.findByCondition(
                null,
                null,
                null,
                null,
                null,
                null
        );

        // 找到最新的 UPDATE 操作日志
        com.gsms.gsms.model.entity.OperationLog updateLog = logs.stream()
                .filter(log -> log.getOperationType().name().equals("UPDATE"))
                .filter(log -> log.getBusinessType() != null && log.getBusinessType().equals("USER"))
                .filter(log -> log.getBusinessId().equals(createdUserId))
                .filter(log -> log.getOperationContent() != null && log.getOperationContent().contains("更新后的昵称"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到 UPDATE 操作日志"));

        // 验证 business_type 和 business_id
        assert updateLog.getBusinessType().equals("USER") : "business_type 应该是 USER";
        assert updateLog.getBusinessId().equals(createdUserId) : "business_id 应该匹配";

        // 验证 old_value 包含更新前的数据
        assert updateLog.getOldValue() != null : "UPDATE 操作的 old_value 不应该为 null";
        com.fasterxml.jackson.databind.JsonNode oldNode = objectMapper.readTree(updateLog.getOldValue());
        assert oldNode.has("id") : "old_value JSON 应该包含 id 字段";
        assert oldNode.has("username") : "old_value JSON 应该包含 username 字段";
        assert oldNode.get("username").asText().equals("logtestuser") : "username 应该是 logtestuser";

        // 验证 new_value 包含更新后的数据
        assert updateLog.getNewValue() != null : "UPDATE 操作的 new_value 不应该为 null";
        JsonNode newNode = objectMapper.readTree(updateLog.getNewValue());
        assert newNode.has("id") : "new_value JSON 应该包含 id 字段";
        assert newNode.has("nickname") : "new_value JSON 应该包含 nickname 字段";
        assert newNode.get("nickname").asText().equals("更新后的昵称") : "nickname 应该是更新后的昵称";
        assert newNode.get("email").asText().equals("updated@example.com") : "email 应该是 updated@example.com";

        System.out.println("✅ UPDATE 操作日志验证通过");
        System.out.println("   - business_type: " + updateLog.getBusinessType());
        System.out.println("   - business_id: " + updateLog.getBusinessId());
        System.out.println("   - old_value (nickname): " + oldNode.get("nickname").asText());
        System.out.println("   - new_value (nickname): " + newNode.get("nickname").asText());
    }

    @Test
    @DisplayName("测试 DELETE 用户的操作日志 - old_value 包含 JSON，new_value 为 null")
    void testDeleteUser_OperationLog() throws Exception {
        // Given - 先创建一个待删除的用户
        UserCreateReq createReq = new UserCreateReq();
        createReq.setUsername("deletetest");
        createReq.setPassword("Test123");
        createReq.setNickname("待删除用户");

        com.gsms.gsms.dto.user.UserInfoResp userResp = userService.create(createReq);
        Long userIdToDelete = userResp.getId();

        // When - 删除用户
        mockMvc.perform(delete("/api/users/" + userIdToDelete)
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Then - 查询操作日志
        java.util.List<com.gsms.gsms.model.entity.OperationLog> logs = operationLogMapper.findByCondition(
                null,
                null,
                null,
                null,
                null,
                null
        );

        // 找到最新的 DELETE 操作日志
        com.gsms.gsms.model.entity.OperationLog deleteLog = logs.stream()
                .filter(log -> log.getOperationType().name().equals("DELETE"))
                .filter(log -> log.getBusinessType() != null && log.getBusinessType().equals("USER"))
                .filter(log -> log.getBusinessId().equals(userIdToDelete))
                .filter(log -> log.getOperationContent() != null && log.getOperationContent().contains("deletetest"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到 DELETE 操作日志"));

        // 验证 business_type 和 business_id
        assert deleteLog.getBusinessType().equals("USER") : "business_type 应该是 USER";
        assert deleteLog.getBusinessId().equals(userIdToDelete) : "business_id 应该匹配";

        // 验证 old_value 包含删除前的数据
        assert deleteLog.getOldValue() != null : "DELETE 操作的 old_value 不应该为 null";
        JsonNode oldNode = objectMapper.readTree(deleteLog.getOldValue());
        assert oldNode.has("id") : "old_value JSON 应该包含 id 字段";
        assert oldNode.has("username") : "old_value JSON 应该包含 username 字段";
        assert oldNode.get("username").asText().equals("deletetest") : "username 应该是 deletetest";
        assert oldNode.has("nickname") : "old_value JSON 应该包含 nickname 字段";

        // 验证 new_value 为 null（DELETE 操作没有新值）
        assert deleteLog.getNewValue() == null : "DELETE 操作的 new_value 应该为 null";

        System.out.println("✅ DELETE 操作日志验证通过");
        System.out.println("   - business_type: " + deleteLog.getBusinessType());
        System.out.println("   - business_id: " + deleteLog.getBusinessId());
        System.out.println("   - old_value (username): " + oldNode.get("username").asText());
        System.out.println("   - new_value: " + deleteLog.getNewValue());
    }
}
