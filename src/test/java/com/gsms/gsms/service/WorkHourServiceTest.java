package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.WorkHour;
import com.gsms.gsms.domain.enums.WorkHourStatus;
import com.gsms.gsms.repository.WorkHourMapper;
import com.gsms.gsms.service.impl.WorkHourServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 工时记录服务测试类
 */
class WorkHourServiceTest {

    @Mock
    private WorkHourMapper workHourMapper;

    @InjectMocks
    private WorkHourServiceImpl workHourService;

    private WorkHour testWorkHour;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testWorkHour = new WorkHour();
        testWorkHour.setId(1L);
        testWorkHour.setUserId(1L);
        testWorkHour.setProjectId(1L);
        testWorkHour.setTaskId(1L);
        testWorkHour.setWorkDate(new Date());
        testWorkHour.setHours(new BigDecimal("8.00"));
        testWorkHour.setContent("开发任务");
        testWorkHour.setStatus(WorkHourStatus.CONFIRMED);
    }

    @Test
    void testGetWorkHourById() {
        // Given
        when(workHourMapper.selectById(1L)).thenReturn(testWorkHour);

        // When
        WorkHour result = workHourService.getWorkHourById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(workHourMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetWorkHoursByUserId() {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourMapper.selectByUserId(1L)).thenReturn(workHours);

        // When
        List<WorkHour> result = workHourService.getWorkHoursByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        verify(workHourMapper, times(1)).selectByUserId(1L);
    }

    @Test
    void testGetWorkHoursByProjectId() {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourMapper.selectByProjectId(1L)).thenReturn(workHours);

        // When
        List<WorkHour> result = workHourService.getWorkHoursByProjectId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProjectId());
        verify(workHourMapper, times(1)).selectByProjectId(1L);
    }

    @Test
    void testGetWorkHoursByCondition() {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        Date startDate = new Date();
        Date endDate = new Date();
        when(workHourMapper.selectByCondition(1L, 1L, startDate, endDate)).thenReturn(workHours);

        // When
        List<WorkHour> result = workHourService.getWorkHoursByCondition(1L, 1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workHourMapper, times(1)).selectByCondition(1L, 1L, startDate, endDate);
    }

    @Test
    void testCreateWorkHour() {
        // Given
        when(workHourMapper.insert(any(WorkHour.class))).thenReturn(1);

        // When
        WorkHour result = workHourService.createWorkHour(testWorkHour);

        // Then
        assertNotNull(result);
        verify(workHourMapper, times(1)).insert(testWorkHour);
    }

    @Test
    void testUpdateWorkHour() {
        // Given
        when(workHourMapper.selectById(1L)).thenReturn(testWorkHour);
        when(workHourMapper.update(any(WorkHour.class))).thenReturn(1);
        when(workHourMapper.selectById(1L)).thenReturn(testWorkHour);

        // When
        WorkHour result = workHourService.updateWorkHour(testWorkHour);

        // Then
        assertNotNull(result);
        verify(workHourMapper, times(1)).update(testWorkHour);
    }

    @Test
    void testDeleteWorkHour() {
        // Given
        when(workHourMapper.selectById(1L)).thenReturn(testWorkHour);
        when(workHourMapper.deleteById(1L)).thenReturn(1);

        // When
        workHourService.deleteWorkHour(1L);

        // Then
        verify(workHourMapper, times(1)).deleteById(1L);
    }
}