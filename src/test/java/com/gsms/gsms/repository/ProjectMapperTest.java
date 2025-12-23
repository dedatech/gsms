package com.gsms.gsms.repository;

import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.config.TestMyBatisConfig;
import com.gsms.gsms.domain.enums.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 项目Mapper测试类
 * 注意：此测试需要真实的数据库连接，会在事务中执行并回滚
 */
@SpringBootTest
@Import(TestMyBatisConfig.class)
@ActiveProfiles("test")
@Transactional
class ProjectMapperTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Test
    void testSelectById() {
        // Given
        Project project = new Project();
        project.setName("测试项目_Mapper");
        project.setCode("TEST-MAPPER-001");
        project.setDescription("这是一个Mapper测试项目");
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.SUSPENDED);
        project.setCreateUserId(1L);
        
        // 插入测试数据
        projectMapper.insert(project);
        assertNotNull(project.getId());

        // When
        Project result = projectMapper.selectById(project.getId());

        // Then
        assertNotNull(result);
        assertEquals("测试项目_Mapper", result.getName());
        assertEquals("TEST-MAPPER-001", result.getCode());
    }

    @Test
    void testSelectAll() {
        // Given
        Project project1 = new Project();
        project1.setName("测试项目_Mapper_1");
        project1.setCode("TEST-MAPPER-001");
        project1.setDescription("第一个Mapper测试项目");
        project1.setManagerId(1L);
        project1.setStatus(ProjectStatus.IN_PROGRESS);
        project1.setCreateUserId(1L);
        
        Project project2 = new Project();
        project2.setName("测试项目_Mapper_2");
        project2.setCode("TEST-MAPPER-002");
        project2.setDescription("第二个Mapper测试项目");
        project2.setManagerId(1L);
        project2.setStatus(ProjectStatus.IN_PROGRESS);
        project2.setCreateUserId(1L);
        
        // 插入测试数据
        projectMapper.insert(project1);
        projectMapper.insert(project2);

        // When
        List<Project> result = projectMapper.selectAll();

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 2);
    }

    @Test
    void testSelectByCondition() {
        // Given
        Project project = new Project();
        project.setName("条件查询测试项目");
        project.setCode("TEST-CONDITION-001");
        project.setDescription("条件查询测试");
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setCreateUserId(1L);
        
        // 插入测试数据
        projectMapper.insert(project);

        // When
        List<Project> result = projectMapper.selectByCondition("条件查询", 1);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("条件查询测试项目", result.get(0).getName());
    }

    @Test
    void testInsert() {
        // Given
        Project project = new Project();
        project.setName("插入测试项目");
        project.setCode("INSERT-TEST-001");
        project.setDescription("插入测试");
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.SUSPENDED);
        project.setCreateUserId(1L);

        // When
        int result = projectMapper.insert(project);

        // Then
        assertEquals(1, result);
        assertNotNull(project.getId());
        assertTrue(project.getId() > 0);
    }

    @Test
    void testUpdate() {
        // Given
        Project project = new Project();
        project.setName("更新前项目");
        project.setCode("UPDATE-BEFORE-001");
        project.setDescription("更新前描述");
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setCreateUserId(1L);
        
        // 插入测试数据
        projectMapper.insert(project);
        
        // 修改数据
        project.setName("更新后项目");
        project.setDescription("更新后描述");

        // When
        int result = projectMapper.update(project);

        // Then
        assertEquals(1, result);
        
        // 验证更新结果
        Project updatedProject = projectMapper.selectById(project.getId());
        assertEquals("更新后项目", updatedProject.getName());
        assertEquals("更新后描述", updatedProject.getDescription());
    }

    @Test
    void testDeleteById() {
        // Given
        Project project = new Project();
        project.setName("删除测试项目");
        project.setCode("DELETE-TEST-001");
        project.setDescription("删除测试");
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.SUSPENDED);
        project.setCreateUserId(1L);
        
        // 插入测试数据
        projectMapper.insert(project);
        assertNotNull(project.getId());

        // When
        int result = projectMapper.deleteById(project.getId());

        // Then
        assertEquals(1, result);
        
        // 验证删除结果
        Project deletedProject = projectMapper.selectById(project.getId());
        assertNull(deletedProject);
    }
}