package com.gsms.gsms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.UserStatus;
import com.gsms.gsms.service.impl.OperationLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 操作日志服务单元测试
 * 验证 JSON 序列化功能
 */
class OperationLogServiceTest {

    @Test
    void testObjectMapperConfiguration() {
        // Given - 创建 OperationLogServiceImpl 实例
        OperationLogServiceImpl service = new OperationLogServiceImpl(null);

        // When - 通过反射获取 ObjectMapper
        ObjectMapper objectMapper = (ObjectMapper) ReflectionTestUtils.getField(service, "objectMapper");

        // Then - 验证 ObjectMapper 配置
        assertNotNull(objectMapper, "ObjectMapper 不应该为 null");

        // Given - 测试用户对象
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("测试用户");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(UserStatus.NORMAL);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // When - 序列化为 JSON
        String json = null;
        try {
            json = objectMapper.writeValueAsString(user);
        } catch (Exception e) {
            fail("JSON 序列化失败: " + e.getMessage());
        }

        // Then - 验证 JSON 格式
        assertNotNull(json, "JSON 字符串不应该为 null");
        assertTrue(json.contains("\"username\":\"testuser\"") || json.contains("\"username\": \"testuser\""),
                "JSON 应该包含 username 字段");
        assertTrue(json.contains("\"nickname\":\"测试用户\"") || json.contains("\"nickname\": \"测试用户\""),
                "JSON 应该包含 nickname 字段");

        // 解析 JSON 验证结构
        try {
            JsonNode node = objectMapper.readTree(json);
            assertTrue(node.has("id"), "JSON 应该有 id 字段");
            assertTrue(node.has("username"), "JSON 应该有 username 字段");
            assertTrue(node.has("nickname"), "JSON 应该有 nickname 字段");
            assertTrue(node.has("email"), "JSON 应该有 email 字段");
            assertEquals(1, node.get("id").asLong(), "id 应该是 1");
            assertEquals("testuser", node.get("username").asText(), "username 应该是 testuser");
            assertEquals("测试用户", node.get("nickname").asText(), "nickname 应该是 测试用户");

            System.out.println("✅ JSON 序列化测试通过");
            System.out.println("   序列化结果: " + json);
        } catch (Exception e) {
            fail("JSON 解析失败: " + e.getMessage());
        }
    }

    @Test
    void testNullObjectSerialization() {
        // Given
        OperationLogServiceImpl service = new OperationLogServiceImpl(null);
        ObjectMapper objectMapper = (ObjectMapper) ReflectionTestUtils.getField(service, "objectMapper");

        // When - 序列化 null 对象
        String json = null;
        try {
            json = objectMapper.writeValueAsString(null);
        } catch (Exception e) {
            fail("null 序列化失败: " + e.getMessage());
        }

        // Then
        assertEquals("null", json, "null 对象序列化应该返回字符串 'null'");
        System.out.println("✅ null 序列化测试通过");
    }
}
