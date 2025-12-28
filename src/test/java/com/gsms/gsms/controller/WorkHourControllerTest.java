package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.domain.entity.WorkHour;
import com.gsms.gsms.domain.enums.WorkHourStatus;
import com.gsms.gsms.dto.workhour.WorkHourCreateReq;
import com.gsms.gsms.dto.workhour.WorkHourUpdateReq;
import com.gsms.gsms.infra.config.JwtInterceptor;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.WorkHourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;
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

    @MockBean
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    private WorkHour testWorkHour;
    private SimpleDateFormat dateFormat;
    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        testWorkHour = new WorkHour();
        testWorkHour.setId(1L);
        testWorkHour.setUserId(1L);
        testWorkHour.setProjectId(1L);
        testWorkHour.setTaskId(1L);
        testWorkHour.setWorkDate(new Date());
        testWorkHour.setHours(new BigDecimal("8.00"));
        testWorkHour.setContent("开发任务");
        testWorkHour.setStatus(WorkHourStatus.SAVED);
        
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        // 生成测试用的JWT Token
        testToken = "test.jwt.token";
        
        // Mock JWT拦截器，让所有请求通过
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void testGetWorkHourById_Success() throws Exception {
        // Given
        when(workHourService.getWorkHourById(1L)).thenReturn(testWorkHour);

        // When & Then
        mockMvc.perform(get("/api/work-hours/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetWorkHourById_NotFound() throws Exception {
        // Given
        when(workHourService.getWorkHourById(1L)).thenThrow(new RuntimeException("工时记录不存在"));

        // When & Then
        mockMvc.perform(get("/api/work-hours/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void testGetWorkHoursByUserId() throws Exception {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourService.getWorkHoursByCondition(1L, null, null, null, null)).thenReturn(workHours);

        // When & Then
        mockMvc.perform(get("/api/work-hours/user/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetWorkHoursByProjectId() throws Exception {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourService.getWorkHoursByCondition(null, 1L, null, null, null)).thenReturn(workHours);

        // When & Then
        mockMvc.perform(get("/api/work-hours/project/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetWorkHoursByCondition() throws Exception {
        // Given
        List<WorkHour> workHours = Arrays.asList(testWorkHour);
        when(workHourService.getWorkHoursByCondition(1L, 1L, null, null, null)).thenReturn(workHours);
        
        // When & Then
        mockMvc.perform(get("/api/work-hours/search")
                .header("Authorization", "Bearer " + testToken)
                .param("userId", "1")
                .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testCreateWorkHour_Success() throws Exception {
        // Given
        WorkHourCreateReq req = new WorkHourCreateReq();
        req.setProjectId(1L);
        req.setTaskId(1L);
        req.setWorkDate(new Date());
        req.setHours(new BigDecimal("8.00"));
        req.setContent("开发任务");
        
        when(workHourService.createWorkHour(any(WorkHour.class))).thenReturn(testWorkHour);

        // When & Then
        try (MockedStatic<UserContext> mockedContext = mockStatic(UserContext.class)) {
            mockedContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            
            mockMvc.perform(post("/api/work-hours")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Test
    void testUpdateWorkHour_Success() throws Exception {
        // Given
        WorkHourUpdateReq req = new WorkHourUpdateReq();
        req.setId(1L);
        req.setProjectId(1L);
        req.setTaskId(1L);
        req.setWorkDate(new Date());
        req.setHours(new BigDecimal("8.00"));
        req.setContent("开发任务");
        req.setStatus(WorkHourStatus.SAVED);
        
        when(workHourService.updateWorkHour(any(WorkHour.class))).thenReturn(testWorkHour);

        // When & Then
        try (MockedStatic<UserContext> mockedContext = mockStatic(UserContext.class)) {
            mockedContext.when(UserContext::getCurrentUserId).thenReturn(1L);
            
            mockMvc.perform(put("/api/work-hours")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Test
    void testDeleteWorkHour_Success() throws Exception {
        // Given
        doNothing().when(workHourService).deleteWorkHour(1L);

        // When & Then
        mockMvc.perform(delete("/api/work-hours/1")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("工时记录删除成功"));
    }
}