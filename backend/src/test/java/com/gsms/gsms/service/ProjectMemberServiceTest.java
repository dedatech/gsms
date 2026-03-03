package com.gsms.gsms.service;

import com.gsms.gsms.dto.project.ProjectMemberResp;
import com.gsms.gsms.model.entity.ProjectMember;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.ProjectMemberRole;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.model.enums.ProjectStatus;
import com.gsms.gsms.model.enums.errorcode.ProjectErrorCode;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.service.impl.ProjectMemberServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 项目成员服务测试类
 * 测试项目成员的查询、添加、删除、排序等功能
 */
@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthService authService;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ProjectMemberServiceImpl projectMemberService;

    private MockedStatic<UserContext> userContextMock;

    private Project testProject;
    private ProjectMember managerMember;
    private ProjectMember normalMember;
    private ProjectMember readonlyMember;
    private User testUser;

    private static final Long TEST_PROJECT_ID = 1L;
    private static final Long CURRENT_USER_ID = 100L;
    private static final Long TEST_USER_ID_1 = 1L;
    private static final Long TEST_USER_ID_2 = 2L;
    private static final Long TEST_USER_ID_3 = 3L;

    @BeforeEach
    void setUp() {
        // Mock UserContext
        userContextMock = mockStatic(UserContext.class);
        when(UserContext.getCurrentUserId()).thenReturn(CURRENT_USER_ID);

        // 准备测试项目
        testProject = new Project();
        testProject.setId(TEST_PROJECT_ID);
        testProject.setName("测试项目");
        testProject.setCode("TEST");
        testProject.setStatus(ProjectStatus.fromCode(1));
        testProject.setCreateTime(LocalDateTime.now());
        testProject.setUpdateTime(LocalDateTime.now());

        // 准备测试用户
        testUser = new User();
        testUser.setId(TEST_USER_ID_1);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        // 准备项目经理成员
        managerMember = new ProjectMember();
        managerMember.setId(1L);
        managerMember.setProjectId(TEST_PROJECT_ID);
        managerMember.setUserId(TEST_USER_ID_1);
        managerMember.setRoleType(ProjectMemberRole.PROJECT_MANAGER.getCode());
        managerMember.setCreateTime(LocalDateTime.now());
        managerMember.setUpdateTime(LocalDateTime.now());

        // 准备普通成员
        normalMember = new ProjectMember();
        normalMember.setId(2L);
        normalMember.setProjectId(TEST_PROJECT_ID);
        normalMember.setUserId(TEST_USER_ID_2);
        normalMember.setRoleType(ProjectMemberRole.MEMBER.getCode());
        normalMember.setCreateTime(LocalDateTime.now());
        normalMember.setUpdateTime(LocalDateTime.now());

        // 准备只读访客成员
        readonlyMember = new ProjectMember();
        readonlyMember.setId(3L);
        readonlyMember.setProjectId(TEST_PROJECT_ID);
        readonlyMember.setUserId(TEST_USER_ID_3);
        readonlyMember.setRoleType(ProjectMemberRole.READ_ONLY.getCode());
        readonlyMember.setCreateTime(LocalDateTime.now());
        readonlyMember.setUpdateTime(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        if (userContextMock != null) {
            userContextMock.close();
        }
    }

    // ========== 查询成员列表测试 ==========

    @Test
    void testListMembersByProjectId_Success_AsSystemUser() {
        // Arrange
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(managerMember, normalMember, readonlyMember));

        // Act
        List<ProjectMember> result = projectMemberService.listMembersByProjectId(TEST_PROJECT_ID);

        // Assert
        assertEquals(3, result.size());
        verify(authService).canViewAllProjects(CURRENT_USER_ID);
        verify(projectMemberMapper).selectMembersByProjectId(TEST_PROJECT_ID);
    }

    @Test
    void testListMembersByProjectId_Success_AsProjectMember() {
        // Arrange
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(false);
        when(projectMemberMapper.selectUserIdsByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(CURRENT_USER_ID, TEST_USER_ID_1, TEST_USER_ID_2));
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(managerMember, normalMember));

        // Act
        List<ProjectMember> result = projectMemberService.listMembersByProjectId(TEST_PROJECT_ID);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testListMembersByProjectId_Forbidden_NotMember() {
        // Arrange
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(false);
        when(projectMemberMapper.selectUserIdsByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(TEST_USER_ID_1, TEST_USER_ID_2)); // 不包含当前用户

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            projectMemberService.listMembersByProjectId(TEST_PROJECT_ID);
        });
    }

    @Test
    void testListMembersRespByProjectId_Success_WithSorting() {
        // Arrange
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        // 返回乱序的成员列表
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(readonlyMember, normalMember, managerMember));
        when(cacheService.getUserNicknameById(TEST_USER_ID_1)).thenReturn("用户1");
        when(cacheService.getUserNicknameById(TEST_USER_ID_2)).thenReturn("用户2");
        when(cacheService.getUserNicknameById(TEST_USER_ID_3)).thenReturn("用户3");

        // Act
        List<ProjectMemberResp> result = projectMemberService.listMembersRespByProjectId(TEST_PROJECT_ID);

        // Assert - 验证排序：项目经理 > 普通成员 > 只读访客
        assertEquals(3, result.size());
        assertEquals(ProjectMemberRole.PROJECT_MANAGER.getCode(), result.get(0).getRoleType());
        assertEquals(ProjectMemberRole.MEMBER.getCode(), result.get(1).getRoleType());
        assertEquals(ProjectMemberRole.READ_ONLY.getCode(), result.get(2).getRoleType());
    }

    // ========== 添加成员测试 ==========

    @Test
    void testAddMembers_Success() {
        // Arrange
        List<Long> newUserIds = Arrays.asList(TEST_USER_ID_2, TEST_USER_ID_3);
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        when(projectMemberMapper.selectUserIdsByProjectId(TEST_PROJECT_ID))
                .thenReturn(Collections.singletonList(TEST_USER_ID_1));
        when(userMapper.selectById(TEST_USER_ID_2)).thenReturn(testUser);
        when(userMapper.selectById(TEST_USER_ID_3)).thenReturn(testUser);
        when(projectMemberMapper.insertProjectMember(anyLong(), anyLong(), anyInt(), anyLong()))
                .thenReturn(1);

        // Act
        projectMemberService.addMembers(TEST_PROJECT_ID, newUserIds, ProjectMemberRole.MEMBER.getCode());

        // Assert
        verify(projectMemberMapper, times(2)).insertProjectMember(
                eq(TEST_PROJECT_ID), anyLong(), eq(ProjectMemberRole.MEMBER.getCode()), eq(CURRENT_USER_ID));
    }

    @Test
    void testAddMembers_ProjectNotFound() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            projectMemberService.addMembers(TEST_PROJECT_ID, Collections.singletonList(TEST_USER_ID_2), 2);
        });
    }

    @Test
    void testAddMembers_Forbidden_NotMember() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(false);
        when(projectMemberMapper.selectUserIdsByProjectId(TEST_PROJECT_ID))
                .thenReturn(Collections.emptyList()); // 当前用户不是项目成员

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            projectMemberService.addMembers(TEST_PROJECT_ID, Collections.singletonList(TEST_USER_ID_2), 2);
        });
    }

    @Test
    void testAddMembers_SkipsExistingMembers() {
        // Arrange
        List<Long> newUserIds = Arrays.asList(TEST_USER_ID_1, TEST_USER_ID_2); // 用户1已存在
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        when(projectMemberMapper.selectUserIdsByProjectId(TEST_PROJECT_ID))
                .thenReturn(Collections.singletonList(TEST_USER_ID_1)); // 用户1已存在
        when(userMapper.selectById(TEST_USER_ID_2)).thenReturn(testUser);
        when(projectMemberMapper.insertProjectMember(anyLong(), anyLong(), anyInt(), anyLong()))
                .thenReturn(1);

        // Act
        projectMemberService.addMembers(TEST_PROJECT_ID, newUserIds, 2);

        // Assert - 应该只为用户2插入，跳过已存在的用户1
        verify(projectMemberMapper, times(1)).insertProjectMember(
                eq(TEST_PROJECT_ID), eq(TEST_USER_ID_2), eq(2), eq(CURRENT_USER_ID));
    }

    // ========== 删除成员测试 ==========

    @Test
    void testRemoveMember_Success_AsProjectManager() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(false);
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(managerMember, normalMember)); // 当前用户是项目经理
        when(taskMapper.countUnfinishedTasksByUserAndProject(TEST_PROJECT_ID, TEST_USER_ID_2))
                .thenReturn(0); // 没有未完成的任务
        when(projectMemberMapper.deleteProjectMember(TEST_PROJECT_ID, TEST_USER_ID_2))
                .thenReturn(1);

        // 设置当前用户为项目经理
        userContextMock.close();
        userContextMock = mockStatic(UserContext.class);
        when(UserContext.getCurrentUserId()).thenReturn(TEST_USER_ID_1); // 项目经理ID

        // Act
        projectMemberService.removeMember(TEST_PROJECT_ID, TEST_USER_ID_2);

        // Assert
        verify(projectMemberMapper).deleteProjectMember(TEST_PROJECT_ID, TEST_USER_ID_2);
    }

    @Test
    void testRemoveMember_Success_AsSystemUser() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true); // 系统用户
        when(taskMapper.countUnfinishedTasksByUserAndProject(TEST_PROJECT_ID, TEST_USER_ID_2))
                .thenReturn(0);
        when(projectMemberMapper.deleteProjectMember(TEST_PROJECT_ID, TEST_USER_ID_2))
                .thenReturn(1);

        // Act
        projectMemberService.removeMember(TEST_PROJECT_ID, TEST_USER_ID_2);

        // Assert
        verify(projectMemberMapper).deleteProjectMember(TEST_PROJECT_ID, TEST_USER_ID_2);
    }

    @Test
    void testRemoveMember_Forbidden_NotProjectManager() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(false);
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Collections.singletonList(normalMember)); // 当前用户是普通成员

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectMemberService.removeMember(TEST_PROJECT_ID, TEST_USER_ID_2);
        });
        assertEquals(ProjectErrorCode.MEMBER_NOT_PROJECT_MANAGER.getCode(), exception.getCode());
    }

    @Test
    void testRemoveMember_HasUnfinishedTasks() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        when(taskMapper.countUnfinishedTasksByUserAndProject(TEST_PROJECT_ID, TEST_USER_ID_2))
                .thenReturn(3); // 有3个未完成的任务

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectMemberService.removeMember(TEST_PROJECT_ID, TEST_USER_ID_2);
        });
        assertEquals(ProjectErrorCode.MEMBER_HAS_UNFINISHED_TASKS.getCode(), exception.getCode());
    }

    @Test
    void testRemoveMember_LastProjectManager() {
        // Arrange
        userContextMock.close();
        userContextMock = mockStatic(UserContext.class);
        when(UserContext.getCurrentUserId()).thenReturn(TEST_USER_ID_1); // 项目经理ID

        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(TEST_USER_ID_1)).thenReturn(false);
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Collections.singletonList(managerMember)); // 只有一个项目经理

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            projectMemberService.removeMember(TEST_PROJECT_ID, TEST_USER_ID_1);
        });
        assertEquals(ProjectErrorCode.LAST_PROJECT_MANAGER_CANNOT_REMOVE.getCode(), exception.getCode());
    }

    @Test
    void testRemoveMember_MemberNotFound() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Collections.emptyList()); // 没有找到要删除的成员
        when(taskMapper.countUnfinishedTasksByUserAndProject(TEST_PROJECT_ID, TEST_USER_ID_2))
                .thenReturn(0);
        when(projectMemberMapper.deleteProjectMember(TEST_PROJECT_ID, TEST_USER_ID_2))
                .thenReturn(0); // 删除失败

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            projectMemberService.removeMember(TEST_PROJECT_ID, TEST_USER_ID_2);
        });
    }

    @Test
    void testRemoveMember_AllowRemoveNormalMemberWhenMultipleManagersExist() {
        // Arrange
        userContextMock.close();
        userContextMock = mockStatic(UserContext.class);
        when(UserContext.getCurrentUserId()).thenReturn(TEST_USER_ID_1);

        // 准备两个项目经理
        ProjectMember manager2 = new ProjectMember();
        manager2.setId(4L);
        manager2.setProjectId(TEST_PROJECT_ID);
        manager2.setUserId(4L);
        manager2.setRoleType(ProjectMemberRole.PROJECT_MANAGER.getCode());

        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(TEST_USER_ID_1)).thenReturn(false);
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(managerMember, manager2)); // 有两个项目经理
        when(taskMapper.countUnfinishedTasksByUserAndProject(TEST_PROJECT_ID, TEST_USER_ID_1))
                .thenReturn(0);
        when(projectMemberMapper.deleteProjectMember(TEST_PROJECT_ID, TEST_USER_ID_1))
                .thenReturn(1);

        // Act - 应该允许删除其中一个项目经理（因为还有一个）
        projectMemberService.removeMember(TEST_PROJECT_ID, TEST_USER_ID_1);

        // Assert
        verify(projectMemberMapper).deleteProjectMember(TEST_PROJECT_ID, TEST_USER_ID_1);
    }

    // ========== 更新成员角色测试 ==========

    @Test
    void testUpdateMemberRole_Success() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        when(projectMemberMapper.updateProjectMemberRole(TEST_PROJECT_ID, TEST_USER_ID_2, 3, CURRENT_USER_ID))
                .thenReturn(1);

        // Act
        projectMemberService.updateMemberRole(TEST_PROJECT_ID, TEST_USER_ID_2, 3);

        // Assert
        verify(projectMemberMapper).updateProjectMemberRole(TEST_PROJECT_ID, TEST_USER_ID_2, 3, CURRENT_USER_ID);
    }

    @Test
    void testUpdateMemberRole_MemberNotFound() {
        // Arrange
        when(projectMapper.selectById(TEST_PROJECT_ID)).thenReturn(testProject);
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        when(projectMemberMapper.updateProjectMemberRole(TEST_PROJECT_ID, TEST_USER_ID_2, 3, CURRENT_USER_ID))
                .thenReturn(0);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            projectMemberService.updateMemberRole(TEST_PROJECT_ID, TEST_USER_ID_2, 3);
        });
    }

    // ========== 辅助方法测试 ==========

    @Test
    void testGetRoleOrder() {
        // 这是一个私有方法，通过公共方法的行为来间接测试
        when(authService.canViewAllProjects(CURRENT_USER_ID)).thenReturn(true);
        // 返回乱序的成员：普通成员、只读访客、项目经理
        when(projectMemberMapper.selectMembersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Arrays.asList(normalMember, readonlyMember, managerMember));
        when(cacheService.getUserNicknameById(anyLong())).thenReturn("用户");

        // Act
        List<ProjectMemberResp> result = projectMemberService.listMembersRespByProjectId(TEST_PROJECT_ID);

        // Assert - 验证排序顺序
        assertEquals(3, result.size());
        assertEquals(ProjectMemberRole.PROJECT_MANAGER.getCode(), result.get(0).getRoleType());
        assertEquals(ProjectMemberRole.MEMBER.getCode(), result.get(1).getRoleType());
        assertEquals(ProjectMemberRole.READ_ONLY.getCode(), result.get(2).getRoleType());
    }
}
