package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.domain.enums.ProjectStatus;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.service.impl.ProjectServiceImpl;
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
 * 项目服务测试类
 */
class ProjectServiceTest {

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("测试项目");
        testProject.setCode("TEST-001");
        testProject.setDescription("这是一个测试项目");
        testProject.setManagerId(1L);
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        testProject.setCreateUserId(1L);
    }

    @Test
    void testGetProjectById() {
        // Given
        when(projectMapper.selectById(1L)).thenReturn(testProject);

        // When
        Project result = projectService.getProjectById(1L);

        // Then
        assertNotNull(result);
        assertEquals("测试项目", result.getName());
        verify(projectMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetAllProjects() {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectMapper.selectAll()).thenReturn(projects);

        // When
        List<Project> result = projectService.getAllProjects();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试项目", result.get(0).getName());
        verify(projectMapper, times(1)).selectAll();
    }

    @Test
    void testGetProjectsByCondition() {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectMapper.selectByCondition("测试", 1)).thenReturn(projects);

        // When
        List<Project> result = projectService.getProjectsByCondition("测试", 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试项目", result.get(0).getName());
        verify(projectMapper, times(1)).selectByCondition("测试", 1);
    }

    @Test
    void testCreateProject() {
        // Given
        when(projectMapper.insert(any(Project.class))).thenReturn(1);

        // When
        Project result = projectService.createProject(testProject);

        // Then
        assertNotNull(result);
        verify(projectMapper, times(1)).insert(testProject);
    }

    @Test
    void testUpdateProject() {
        // Given
        when(projectMapper.selectById(1L)).thenReturn(testProject);
        when(projectMapper.update(any(Project.class))).thenReturn(1);
        when(projectMapper.selectById(1L)).thenReturn(testProject);

        // When
        Project result = projectService.updateProject(testProject);

        // Then
        assertNotNull(result);
        verify(projectMapper, times(1)).update(testProject);
    }

    @Test
    void testDeleteProject() {
        // Given
        when(projectMapper.selectById(1L)).thenReturn(testProject);
        when(projectMapper.deleteById(1L)).thenReturn(1);

        // When
        projectService.deleteProject(1L);

        // Then
        verify(projectMapper, times(1)).deleteById(1L);
    }
}