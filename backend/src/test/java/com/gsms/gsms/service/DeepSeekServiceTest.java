package com.gsms.gsms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.dto.ai.RequirementBreakdownReq;
import com.gsms.gsms.dto.ai.RequirementBreakdownResp;
import com.gsms.gsms.infra.config.DeepSeekConfig;
import com.gsms.gsms.infra.config.DeepSeekProperties;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * DeepSeek 服务测试
 *
 * 注意：这个测试会调用真实的 DeepSeek API
 * 运行前请确保已在 application.yml 中配置了有效的 API Key
 */
class DeepSeekServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekServiceTest.class);

    private DeepSeekService deepSeekService;
    private RequirementBreakdownService requirementBreakdownService;

    @BeforeEach
    void setUp() {
        // 创建配置
        DeepSeekProperties properties = new DeepSeekProperties();
        properties.setKey("sk-cafb7c4bbc884fd9bcb0dd2fcda0f2ea");
        properties.setUrl("https://api.deepseek.com/v1/chat/completions");
        properties.setModel("deepseek-chat");
        properties.setTimeout(60000);
        properties.setMaxRetries(3);

        // 创建 HTTP 客户端
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        // 创建 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // 忽略未知属性
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 创建服务
        deepSeekService = new DeepSeekService(properties, httpClient, objectMapper);
        requirementBreakdownService = new RequirementBreakdownService(deepSeekService, objectMapper);
    }

    @Test
    void testSimpleChat() {
        logger.info("=== 测试简单对话 ===");

        String response = deepSeekService.chat(
                "你是一个友好的助手。",
                "你好，请用一句话介绍你自己。"
        );

        logger.info("DeepSeek 响应: {}", response);
        System.out.println("\n========== DeepSeek 响应 ==========");
        System.out.println(response);
        System.out.println("===================================\n");
    }

    @Test
    void testRequirementBreakdown() {
        logger.info("=== 测试需求拆分 ===");

        RequirementBreakdownReq request = new RequirementBreakdownReq();
        request.setRequirement("开发一个简单的用户管理功能，包括用户列表查询、新增用户、编辑用户、删除用户功能");
        request.setProjectType("Web应用");
        request.setTeamSize(3);

        RequirementBreakdownResp response = requirementBreakdownService.breakdownRequirement(request);

        logger.info("需求拆分结果:");
        logger.info("  概述: {}", response.getSummary());
        logger.info("  子任务数量: {}", response.getSubTasks() != null ? response.getSubTasks().size() : 0);
        logger.info("  总预估人天: {}", response.getTotalEstimatedDays());
        logger.info("  建议团队规模: {}", response.getSuggestedTeamSize());
        logger.info("  建议迭代周期: {} 天", response.getSuggestedIterationDays());

        if (response.getSubTasks() != null) {
            System.out.println("\n========== 子任务列表 ==========");
            for (int i = 0; i < response.getSubTasks().size(); i++) {
                com.gsms.gsms.dto.ai.SubTaskEstimate task = response.getSubTasks().get(i);
                System.out.printf("%d. [%s] %s (预估: %.1f 人天, 优先级: %s)%n",
                        i + 1,
                        task.getTaskType(),
                        task.getTitle(),
                        task.getEstimatedDays(),
                        task.getPriority());
                if (task.getDescription() != null) {
                    System.out.println("   描述: " + task.getDescription());
                }
            }
            System.out.println("=================================\n");
        }

        if (response.getRisks() != null && !response.getRisks().isEmpty()) {
            System.out.println("========== 风险提示 ==========");
            response.getRisks().forEach(risk -> System.out.println("⚠️  " + risk));
            System.out.println("==============================\n");
        }

        if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
            System.out.println("========== 技术建议 ==========");
            response.getSuggestions().forEach(suggestion -> System.out.println("💡 " + suggestion));
            System.out.println("==============================\n");
        }
    }
}
