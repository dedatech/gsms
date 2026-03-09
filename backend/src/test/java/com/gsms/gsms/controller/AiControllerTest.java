package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.dto.ai.RequirementBreakdownReq;
import com.gsms.gsms.infra.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AI 功能控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testBreakdownRequirement() throws Exception {
        // 注意：这个测试需要配置 DEEPSEEK_API_KEY 环境变量
        // 如果没有配置，这个测试会失败

        RequirementBreakdownReq request = new RequirementBreakdownReq();
        request.setRequirement("开发一个用户登录功能，包括用户名密码登录、记住密码、找回密码功能");
        request.setProjectType("Web应用");
        request.setTeamSize(3);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ai/breakdown-requirement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.subTasks").isArray())
                .andExpect(jsonPath("$.data.totalEstimatedDays").exists());
    }

    @Test
    void testCheckStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/ai/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testBreakdownRequirementValidation() throws Exception {
        // 测试参数校验
        RequirementBreakdownReq request = new RequirementBreakdownReq();
        // requirement 为空，应该触发校验错误

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ai/breakdown-requirement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
