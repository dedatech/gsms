package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.domain.enums.UserStatus;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 */
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setNickname("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(UserStatus.NORMAL);
    }

    @Test
    void testGetUserById() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // When
        User result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetUserByUsername() {
        // Given
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When
        User result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).selectByUsername("testuser");
    }

    @Test
    void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userMapper.selectAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userMapper, times(1)).selectAll();
    }

    @Test
    void testCreateUser() {
        // Given
        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).insert(testUser);
    }

    @Test
    void testUpdateUser() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.update(any(User.class))).thenReturn(1);

        // When
        User result = userService.updateUser(testUser);

        // Then
        assertNotNull(result);
        verify(userMapper, times(1)).update(testUser);
        verify(userMapper, times(2)).selectById(1L); // 查询两次：检查存在 + 返回结果
    }

    @Test
    void testDeleteUser() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userMapper, times(1)).selectById(1L);
        verify(userMapper, times(1)).deleteById(1L);
    }

    @Test
    void testLogin_Success() {
        // Given
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When
        User result = userService.login("testuser", "password");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).selectByUsername("testuser");
    }

    @Test
    void testLogin_WrongPassword() {
        // Given
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        // When
        User result = userService.login("testuser", "wrongpassword");

        // Then
        assertNull(result);
        verify(userMapper, times(1)).selectByUsername("testuser");
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // When
        User result = userService.login("nonexistent", "password");

        // Then
        assertNull(result);
        verify(userMapper, times(1)).selectByUsername("nonexistent");
    }
}