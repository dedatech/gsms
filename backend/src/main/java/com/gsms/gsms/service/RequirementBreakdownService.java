package com.gsms.gsms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.dto.ai.RequirementBreakdownReq;
import com.gsms.gsms.dto.ai.RequirementBreakdownResp;
import com.gsms.gsms.dto.ai.SubTaskEstimate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求拆分服务
 */
@Service
public class RequirementBreakdownService {

    private static final Logger logger = LoggerFactory.getLogger(RequirementBreakdownService.class);

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;

    // 系统提示词模板
    private static final String SYSTEM_PROMPT =
        "你是一个资深的项目经理和技术专家，擅长将需求拆分为可执行的子任务并预估工时。\n" +
        "\n" +
        "【重要】请只返回纯 JSON 格式的结果，不要添加任何解释性文字、markdown 代码块标记或其他内容。\n" +
        "\n" +
        "你的任务是：\n" +
        "1. 分析用户的需求描述\n" +
        "2. 将需求拆分为具体的子任务\n" +
        "3. 为每个子任务预估人天（单位：人天）\n" +
        "4. 提供任务类型、优先级、依赖关系等额外信息\n" +
        "5. 识别潜在风险并提供技术建议\n" +
        "\n" +
        "子任务类型包括：\n" +
        "- 需求分析\n" +
        "- 系统设计\n" +
        "- 前端开发\n" +
        "- 后端开发\n" +
        "- 数据库设计\n" +
        "- 接口开发\n" +
        "- 测试\n" +
        "- 部署\n" +
        "- 其他\n" +
        "\n" +
        "优先级：高、中、低\n" +
        "\n" +
        "工时估算标准：\n" +
        "- 简单任务：0.5-1 人天\n" +
        "- 中等任务：1-3 人天\n" +
        "- 复杂任务：3-5 人天\n" +
        "- 非常复杂任务：5+ 人天\n" +
        "\n" +
        "JSON 格式要求：\n" +
        "{\n" +
        "  \"summary\": \"需求概述（1-2句话）\",\n" +
        "  \"subTasks\": [\n" +
        "    {\n" +
        "      \"sequence\": 1,\n" +
        "      \"title\": \"子任务标题\",\n" +
        "      \"description\": \"子任务详细描述\",\n" +
        "      \"estimatedDays\": 2.0,\n" +
        "      \"taskType\": \"后端开发\",\n" +
        "      \"priority\": \"高\",\n" +
        "      \"dependsOn\": null,\n" +
        "      \"notes\": \"备注信息（可选）\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"totalEstimatedDays\": 10.0,\n" +
        "  \"suggestedTeamSize\": 3,\n" +
        "  \"suggestedIterationDays\": 14,\n" +
        "  \"risks\": [\"风险1\", \"风险2\"],\n" +
        "  \"suggestions\": [\"建议1\", \"建议2\"],\n" +
        "  \"notes\": \"其他备注信息\"\n" +
        "}";

    public RequirementBreakdownService(DeepSeekService deepSeekService, ObjectMapper objectMapper) {
        this.deepSeekService = deepSeekService;
        this.objectMapper = objectMapper;
    }

    /**
     * 拆分需求并预估工时
     *
     * @param request 需求拆分请求
     * @return 需求拆分响应
     */
    public RequirementBreakdownResp breakdownRequirement(RequirementBreakdownReq request) {
        // 构建用户提示词
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("请帮我拆分以下需求并预估工时：\n\n");
        userPrompt.append("需求描述：").append(request.getRequirement()).append("\n");

        if (request.getProjectType() != null && !request.getProjectType().isEmpty()) {
            userPrompt.append("项目类型：").append(request.getProjectType()).append("\n");
        }

        if (request.getTeamSize() != null) {
            userPrompt.append("团队规模：").append(request.getTeamSize()).append("人\n");
        }

        if (request.getExpectedDays() != null) {
            userPrompt.append("期望完成时间：").append(request.getExpectedDays()).append("天\n");
        }

        userPrompt.append("\n请按照系统提示词要求的 JSON 格式返回结果。");

        logger.info("开始调用 DeepSeek API 拆分需求");
        long startTime = System.currentTimeMillis();

        try {
            // 调用 DeepSeek API
            String response = deepSeekService.chat(SYSTEM_PROMPT, userPrompt.toString());

            logger.debug("DeepSeek API 原始响应: {}", response);

            // 解析 JSON 响应
            RequirementBreakdownResp result = parseResponse(response);

            // 计算总工时（如果 API 返回的总工时不准确，重新计算）
            if (result.getSubTasks() != null) {
                double totalDays = result.getSubTasks().stream()
                        .mapToDouble(task -> task.getEstimatedDays() != null ? task.getEstimatedDays() : 0.0)
                        .sum();
                result.setTotalEstimatedDays(totalDays);
            }

            long endTime = System.currentTimeMillis();
            logger.info("需求拆分完成，耗时: {}ms", endTime - startTime);

            return result;

        } catch (Exception e) {
            logger.error("需求拆分失败: {}", e.getMessage(), e);
            throw new RuntimeException("需求拆分失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 AI 响应
     *
     * @param response AI 返回的 JSON 字符串
     * @return 需求拆分响应
     */
    private RequirementBreakdownResp parseResponse(String response) {
        try {
            // 尝试直接解析
            return objectMapper.readValue(response, RequirementBreakdownResp.class);
        } catch (JsonProcessingException e) {
            logger.warn("直接解析 JSON 失败，尝试提取 JSON 块");

            // 尝试提取 JSON 块（处理可能的 markdown 代码块）
            String jsonContent = extractJson(response);
            try {
                return objectMapper.readValue(jsonContent, RequirementBreakdownResp.class);
            } catch (JsonProcessingException ex) {
                logger.error("提取 JSON 后解析仍然失败: {}", ex.getMessage());

                // 返回一个默认的错误响应
                return createErrorResponse(response);
            }
        }
    }

    /**
     * 从响应中提取 JSON 内容
     */
    private String extractJson(String response) {
        // 查找 ```json 代码块
        int jsonStart = response.indexOf("```json");
        if (jsonStart != -1) {
            jsonStart += 7; // 跳过 "```json"
            int jsonEnd = response.indexOf("```", jsonStart);
            if (jsonEnd != -1) {
                return response.substring(jsonStart, jsonEnd).trim();
            }
        }

        // 查找 ``` 代码块
        int codeStart = response.indexOf("```");
        if (codeStart != -1) {
            codeStart += 3;
            int codeEnd = response.indexOf("```", codeStart);
            if (codeEnd != -1) {
                String content = response.substring(codeStart, codeEnd).trim();
                // 检查是否以 { 开头
                if (content.startsWith("{")) {
                    return content;
                }
            }
        }

        // 查找第一个 { 和最后一个 }
        int braceStart = response.indexOf("{");
        int braceEnd = response.lastIndexOf("}");
        if (braceStart != -1 && braceEnd != -1 && braceEnd > braceStart) {
            return response.substring(braceStart, braceEnd + 1);
        }

        // 如果都找不到，返回原响应
        return response;
    }

    /**
     * 创建错误响应
     */
    private RequirementBreakdownResp createErrorResponse(String rawResponse) {
        RequirementBreakdownResp errorResp = new RequirementBreakdownResp();
        errorResp.setSummary("AI 响应解析失败");
        errorResp.setSubTasks(new ArrayList<>());
        errorResp.setTotalEstimatedDays(0.0);
        errorResp.setNotes("无法解析 AI 响应。原始响应：\n" + rawResponse);
        errorResp.setRisks(java.util.Collections.singletonList("AI 响应格式错误"));
        return errorResp;
    }
}
