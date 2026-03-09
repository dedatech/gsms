package com.gsms.gsms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsms.gsms.dto.deepseek.DeepSeekChatRequest;
import com.gsms.gsms.dto.deepseek.DeepSeekChatResponse;
import com.gsms.gsms.dto.deepseek.DeepSeekMessage;
import com.gsms.gsms.infra.config.DeepSeekProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek API 服务
 */
@Service
public class DeepSeekService {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);

    private final DeepSeekProperties properties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    public DeepSeekService(
            DeepSeekProperties properties,
            OkHttpClient httpClient,
            @Qualifier("deepseekObjectMapper") ObjectMapper objectMapper) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用 DeepSeek API 进行对话
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return API 响应内容
     */
    public String chat(String systemPrompt, String userPrompt) {
        DeepSeekMessage systemMessage = DeepSeekMessage.system(systemPrompt);
        DeepSeekMessage userMessage = DeepSeekMessage.user(userPrompt);

        java.util.List<DeepSeekMessage> messages = new java.util.ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);

        DeepSeekChatRequest request = new DeepSeekChatRequest(
                properties.getModel(),
                messages
        );
        request.setTemperature(0.7);
        request.setMaxTokens(4000); // 增加到 4000，支持更长的响应

        return sendRequest(request);
    }

    /**
     * 调用 DeepSeek API 进行对话（多轮）
     *
     * @param messages 消息列表
     * @return API 响应内容
     */
    public String chat(java.util.List<DeepSeekMessage> messages) {
        DeepSeekChatRequest request = new DeepSeekChatRequest(
                properties.getModel(),
                messages
        );
        request.setTemperature(0.7);
        request.setMaxTokens(4000); // 增加到 4000，支持更长的响应

        return sendRequest(request);
    }

    /**
     * 发送请求到 DeepSeek API
     *
     * @param request 聊天请求
     * @return API 响应内容
     */
    private String sendRequest(DeepSeekChatRequest request) {
        int retryCount = 0;
        int maxRetries = properties.getMaxRetries();

        while (retryCount <= maxRetries) {
            try {
                String jsonBody = objectMapper.writeValueAsString(request);
                logger.debug("DeepSeek API 请求: {}", jsonBody);

                Request httpRequest = new Request.Builder()
                        .url(properties.getUrl())
                        .addHeader("Authorization", "Bearer " + properties.getKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(jsonBody, JSON_TYPE))
                        .build();

                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "无响应体";
                        logger.error("DeepSeek API 调用失败: HTTP {} - {}", response.code(), errorBody);
                        throw new RuntimeException("DeepSeek API 调用失败: HTTP " + response.code() + " - " + errorBody);
                    }

                    String responseBody = response.body() != null ? response.body().string() : "";

                    // 记录响应信息（不记录完整内容，避免日志过长）
                    int responseLength = responseBody.length();
                    logger.debug("DeepSeek API 响应长度: {} 字符", responseLength);

                    if (responseLength > 1000) {
                        logger.debug("DeepSeek API 响应前500字符: {}", responseBody.substring(0, 500));
                        logger.debug("DeepSeek API 响应后500字符: {}", responseBody.substring(responseLength - 500));
                    } else {
                        logger.debug("DeepSeek API 响应: {}", responseBody);
                    }

                    DeepSeekChatResponse chatResponse = objectMapper.readValue(
                            responseBody,
                            DeepSeekChatResponse.class
                    );

                    String content = chatResponse.getFirstContent();
                    if (content == null || content.isEmpty()) {
                        throw new RuntimeException("DeepSeek API 返回内容为空");
                    }

                    return content;
                }

            } catch (IOException e) {
                retryCount++;
                if (retryCount > maxRetries) {
                    logger.error("DeepSeek API 调用失败（重试 {} 次后仍失败）: {}", maxRetries, e.getMessage());
                    throw new RuntimeException("DeepSeek API 调用失败: " + e.getMessage(), e);
                }
                logger.warn("DeepSeek API 调用失败，正在进行第 {} 次重试: {}", retryCount, e.getMessage());
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        throw new RuntimeException("DeepSeek API 调用失败");
    }
}
