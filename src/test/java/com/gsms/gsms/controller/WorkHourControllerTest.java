package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.entity.WorkHour;
import com.gsms.gsms.service.WorkHourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 工时记录控制器测试类
 */
@WebMvcTest(WorkHourController.class)
public class WorkHourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkHourService workHourService;

    @Autowired
    private ObjectMapper objectMapper;

    private WorkHour testWorkHour;
    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setUp() {
        testWorkHour = new WorkHour();
        testWorkHour.setId(1L);
        testWorkHour.setUserId(1L);
        testWorkHour.setProjectId(1L);
        testWorkHour.setTaskId(1L);
        testWorkHour.setWorkDate(new Date());
        testWorkHour.setHours(new BigDecimal("8.00"));
        testWorkHour.setContent("开发任务");
        testWorkHour.setStatus(1);
        
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Test
    void testGetWorkHourById_Success() throws Exception {
        // Given
        when(workHourService.getWorkHourById(1L)).thenReturn(testWorkHour);

        // When & Then
        mockMvc.perform(get("/api/work-hours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetWorkHourById_NotFound() throws Exception {
        // Given
        when(workHourService.getWorkHourById(1L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/work-hours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("工时记录不存在"));
    }

    @Test
    void testGetWorkHoursByUserId() throws Exception {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourService.getWorkHoursByUserId(1L)).thenReturn(workHours);

        // When & Then
        mockMvc.perform(get("/api/work-hours/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetWorkHoursByProjectId() throws Exception {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourService.getWorkHoursByProjectId(1L)).thenReturn(workHours);

        // When & Then
        mockMvc.perform(get("/api/work-hours/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetWorkHoursByCondition() throws Exception {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourService.getWorkHoursByCondition(1L, 1L, null, null)).thenReturn(workHours);

        // When & Then
        mockMvc.perform(get("/api/work-hours/search")
                .param("userId", "1")
                .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testCreateWorkHour_Success() throws Exception {
        // Given
        when(workHourService.createWorkHour(any(WorkHour.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/work-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testWorkHour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("工时记录创建成功"));
    }

    @Test
    void testUpdateWorkHour_Success() throws Exception {
        // Given
        when(workHourService.updateWorkHour(any(WorkHour.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/work-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testWorkHour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("工时记录更新成功"));
    }

    @Test
    void testDeleteWorkHour_Success() throws Exception {
        // Given
        when(workHourService.deleteWorkHour(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/work-hours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("工时记录删除成功"));
    }
}