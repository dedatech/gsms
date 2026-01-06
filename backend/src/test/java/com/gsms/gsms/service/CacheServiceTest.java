package com.gsms.gsms.service;

import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.entity.Department;
import com.gsms.gsms.model.enums.UserStatus;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.repository.DepartmentMapper;
import com.gsms.gsms.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 缓存服务测试类
 * 测试缓存服务的加载、查询、更新、删除功能
 */
@SpringBootTest
class CacheServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private CacheServiceImpl cacheService;

    private User testUser1;
    private User testUser2;
    private Department testDepartment1;
    private Department testDepartment2;

    @BeforeEach
    void setUp() {
        // 重置缓存服务
        cacheService = new CacheServiceImpl(userMapper, departmentMapper);

        // 准备测试数据
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setUsername("testuser1");
        testUser1.setNickname("测试用户1");
        testUser1.setEmail("test1@example.com");
        testUser1.setPhone("13800138001");
        testUser1.setStatus(UserStatus.NORMAL);
        testUser1.setDepartmentId(1L);
        testUser1.setCreateTime(LocalDateTime.now());
        testUser1.setUpdateTime(LocalDateTime.now());

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setNickname("测试用户2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPhone("13800138002");
        testUser2.setStatus(UserStatus.NORMAL);
        testUser2.setDepartmentId(2L);
        testUser2.setCreateTime(LocalDateTime.now());
        testUser2.setUpdateTime(LocalDateTime.now());

        testDepartment1 = new Department();
        testDepartment1.setId(1L);
        testDepartment1.setName("研发部");
        testDepartment1.setParentId(0L);
        testDepartment1.setLevel(1);
        testDepartment1.setSort(1);
        testDepartment1.setCreateTime(LocalDateTime.now());
        testDepartment1.setUpdateTime(LocalDateTime.now());

        testDepartment2 = new Department();
        testDepartment2.setId(2L);
        testDepartment2.setName("产品部");
        testDepartment2.setParentId(0L);
        testDepartment2.setLevel(1);
        testDepartment2.setSort(2);
        testDepartment2.setCreateTime(LocalDateTime.now());
        testDepartment2.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testRefreshUserCache() {
        // 准备模拟数据
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userMapper.selectAll()).thenReturn(users);

        // 执行刷新
        cacheService.refreshUserCache();

        // 验证
        verify(userMapper, times(1)).selectAll();
        assertTrue(cacheService.getUserById(1L).isPresent());
        assertTrue(cacheService.getUserById(2L).isPresent());
        assertEquals("testuser1", cacheService.getUserById(1L).get().getUsername());
        assertEquals("testuser2", cacheService.getUserById(2L).get().getUsername());
    }

    @Test
    void testRefreshDepartmentCache() {
        // 准备模拟数据
        List<Department> departments = Arrays.asList(testDepartment1, testDepartment2);
        when(departmentMapper.selectAll()).thenReturn(departments);

        // 执行刷新
        cacheService.refreshDepartmentCache();

        // 验证
        verify(departmentMapper, times(1)).selectAll();
        assertTrue(cacheService.getDepartmentById(1L).isPresent());
        assertTrue(cacheService.getDepartmentById(2L).isPresent());
        assertEquals("研发部", cacheService.getDepartmentById(1L).get().getName());
        assertEquals("产品部", cacheService.getDepartmentById(2L).get().getName());
    }

    @Test
    void testGetUserById() {
        // 准备缓存
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userMapper.selectAll()).thenReturn(users);
        cacheService.refreshUserCache();

        // 测试获取存在的用户
        Optional<User> result = cacheService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals("testuser1", result.get().getUsername());

        // 测试获取不存在的用户
        result = cacheService.getUserById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserByUsername() {
        // 准备缓存
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userMapper.selectAll()).thenReturn(users);
        cacheService.refreshUserCache();

        // 测试获取存在的用户
        Optional<User> result = cacheService.getUserByUsername("testuser1");
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());

        // 测试获取不存在的用户
        result = cacheService.getUserByUsername("notexist");
        assertFalse(result.isPresent());
    }

    @Test
    void testGetDepartmentById() {
        // 准备缓存
        List<Department> departments = Arrays.asList(testDepartment1, testDepartment2);
        when(departmentMapper.selectAll()).thenReturn(departments);
        cacheService.refreshDepartmentCache();

        // 测试获取存在的部门
        Optional<Department> result = cacheService.getDepartmentById(1L);
        assertTrue(result.isPresent());
        assertEquals("研发部", result.get().getName());

        // 测试获取不存在的部门
        result = cacheService.getDepartmentById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void testPutUser() {
        // 准备缓存
        List<User> users = Arrays.asList(testUser1);
        when(userMapper.selectAll()).thenReturn(users);
        cacheService.refreshUserCache();

        // 添加新用户到缓存
        cacheService.putUser(testUser2);

        // 验证
        assertTrue(cacheService.getUserById(2L).isPresent());
        assertTrue(cacheService.getUserByUsername("testuser2").isPresent());
    }

    @Test
    void testRemoveUser() {
        // 准备缓存
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userMapper.selectAll()).thenReturn(users);
        cacheService.refreshUserCache();

        // 移除用户
        cacheService.removeUser(1L);

        // 验证
        assertFalse(cacheService.getUserById(1L).isPresent());
        assertFalse(cacheService.getUserByUsername("testuser1").isPresent());
        assertTrue(cacheService.getUserById(2L).isPresent());
    }

    @Test
    void testPutDepartment() {
        // 准备缓存
        List<Department> departments = Arrays.asList(testDepartment1);
        when(departmentMapper.selectAll()).thenReturn(departments);
        cacheService.refreshDepartmentCache();

        // 添加新部门到缓存
        cacheService.putDepartment(testDepartment2);

        // 验证
        assertTrue(cacheService.getDepartmentById(2L).isPresent());
    }

    @Test
    void testRemoveDepartment() {
        // 准备缓存
        List<Department> departments = Arrays.asList(testDepartment1, testDepartment2);
        when(departmentMapper.selectAll()).thenReturn(departments);
        cacheService.refreshDepartmentCache();

        // 移除部门
        cacheService.removeDepartment(1L);

        // 验证
        assertFalse(cacheService.getDepartmentById(1L).isPresent());
        assertTrue(cacheService.getDepartmentById(2L).isPresent());
    }

    @Test
    void testGetUserNicknameById() {
        // 准备缓存
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userMapper.selectAll()).thenReturn(users);
        cacheService.refreshUserCache();

        // 测试获取存在的用户昵称
        String nickname = cacheService.getUserNicknameById(1L);
        assertEquals("测试用户1", nickname);

        // 测试获取不存在的用户
        nickname = cacheService.getUserNicknameById(999L);
        assertNull(nickname);
    }

    @Test
    void testGetDepartmentNameById() {
        // 准备缓存
        List<Department> departments = Arrays.asList(testDepartment1, testDepartment2);
        when(departmentMapper.selectAll()).thenReturn(departments);
        cacheService.refreshDepartmentCache();

        // 测试获取存在的部门名称
        String name = cacheService.getDepartmentNameById(1L);
        assertEquals("研发部", name);

        // 测试获取不存在的部门
        name = cacheService.getDepartmentNameById(999L);
        assertNull(name);
    }

    @Test
    void testRefreshAll() {
        // 准备模拟数据
        List<User> users = Arrays.asList(testUser1, testUser2);
        List<Department> departments = Arrays.asList(testDepartment1, testDepartment2);
        when(userMapper.selectAll()).thenReturn(users);
        when(departmentMapper.selectAll()).thenReturn(departments);

        // 执行刷新
        cacheService.refreshAll();

        // 验证
        verify(userMapper, times(1)).selectAll();
        verify(departmentMapper, times(1)).selectAll();
        assertTrue(cacheService.getUserById(1L).isPresent());
        assertTrue(cacheService.getDepartmentById(1L).isPresent());
    }
}
