package com.gsms.gsms.mapper;

import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.UserStatus;
import com.gsms.gsms.repository.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户Mapper测试类
 * 注意：此测试需要真实的数据库连接，会在事务中执行并回滚
 */
@SpringBootTest
@Transactional
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testSelectById() {
        // Given
        User user = new User();
        user.setUsername("testuser_mapper");
        user.setPassword("password");
        user.setNickname("测试用户");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(UserStatus.NORMAL);
        
        // 插入测试数据
        userMapper.insert(user);
        assertNotNull(user.getId());

        // When
        User result = userMapper.selectById(user.getId());

        // Then
        assertNotNull(result);
        assertEquals("testuser_mapper", result.getUsername());
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    void testSelectByUsername() {
        // Given
        User user = new User();
        user.setUsername("testuser_mapper_select");
        user.setPassword("password");
        user.setNickname("测试用户");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(UserStatus.NORMAL);
        
        // 插入测试数据
        userMapper.insert(user);

        // When
        User result = userMapper.selectByUsername("testuser_mapper_select");

        // Then
        assertNotNull(result);
        assertEquals("testuser_mapper_select", result.getUsername());
    }

    @Test
    void testSelectAll() {
        // Given
        User user1 = new User();
        user1.setUsername("testuser_mapper_1");
        user1.setPassword("password");
        user1.setNickname("测试用户1");
        user1.setEmail("test1@example.com");
        user1.setPhone("13800138001");
        user1.setStatus(UserStatus.NORMAL);
        
        User user2 = new User();
        user2.setUsername("testuser_mapper_2");
        user2.setPassword("password");
        user2.setNickname("测试用户2");
        user2.setEmail("test2@example.com");
        user2.setPhone("13800138002");
        user2.setStatus(UserStatus.NORMAL);
        
        // 插入测试数据
        userMapper.insert(user1);
        userMapper.insert(user2);

        // When
        List<User> result = userMapper.selectAll();

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 2);
    }

    @Test
    void testInsert() {
        // Given
        User user = new User();
        user.setUsername("testuser_insert");
        user.setPassword("password");
        user.setNickname("插入测试用户");
        user.setEmail("insert@example.com");
        user.setPhone("13800138003");
        user.setStatus(UserStatus.NORMAL);

        // When
        int result = userMapper.insert(user);

        // Then
        assertEquals(1, result);
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    void testUpdate() {
        // Given
        User user = new User();
        user.setUsername("testuser_update");
        user.setPassword("password");
        user.setNickname("更新前用户");
        user.setEmail("before@example.com");
        user.setPhone("13800138004");
        user.setStatus(UserStatus.NORMAL);
        
        // 插入测试数据
        userMapper.insert(user);
        
        // 修改数据
        user.setNickname("更新后用户");
        user.setEmail("after@example.com");

        // When
        int result = userMapper.update(user);

        // Then
        assertEquals(1, result);
        
        // 验证更新结果
        User updatedUser = userMapper.selectById(user.getId());
        assertEquals("更新后用户", updatedUser.getNickname());
        assertEquals("after@example.com", updatedUser.getEmail());
    }

    @Test
    void testDeleteById() {
        // Given
        User user = new User();
        user.setUsername("testuser_delete");
        user.setPassword("password");
        user.setNickname("删除测试用户");
        user.setEmail("delete@example.com");
        user.setPhone("13800138005");
        user.setStatus(UserStatus.NORMAL);
        
        // 插入测试数据
        userMapper.insert(user);
        assertNotNull(user.getId());

        // When
        int result = userMapper.deleteById(user.getId());

        // Then
        assertEquals(1, result);
        
        // 验证删除结果
        User deletedUser = userMapper.selectById(user.getId());
        assertNull(deletedUser);
    }
}