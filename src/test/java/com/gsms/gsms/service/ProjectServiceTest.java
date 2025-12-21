package com.gsms.gsms.service;

import com.gsms.gsms.entity.Project;
import com.gsms.gsms.mapper.ProjectMapper;
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
        testProject.setStatus(1);
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
        boolean result = projectService.createProject(testProject);

        // Then
        assertTrue(result);
        verify(projectMapper, times(1)).insert(testProject);
    }

    @Test
    void testUpdateProject() {
        // Given
        when(projectMapper.update(any(Project.class))).thenReturn(1);

        // When
        boolean result = projectService.updateProject(testProject);

        // Then
        assertTrue(result);
        verify(projectMapper, times(1)).update(testProject);
    }

    @Test
    void testDeleteProject() {
        // Given
        when(projectMapper.deleteById(1L)).thenReturn(1);

        // When
        boolean result = projectService.deleteProject(1L);

        // Then
        assertTrue(result);
        verify(projectMapper, times(1)).deleteById(1L);
    }
}