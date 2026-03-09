package com.gsms.gsms.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DeepSeek API 聊天请求
 */
public class DeepSeekChatRequest {

    /**
     * 模型名称
     */
    @JsonProperty("model")
    private String model;

    /**
     * 消息列表
     */
    @JsonProperty("messages")
    private List<DeepSeekMessage> messages;

    /**
     * 温度参数（0-1），控制随机性
     */
    @JsonProperty("temperature")
    private Double temperature = 0.7;

    /**
     * 最大生成 token 数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens = 2000;

    /**
     * 响应格式
     */
    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    /**
     * 响应格式（用于强制 JSON 输出）
     */
    public static class ResponseFormat {
        @JsonProperty("type")
        private String type = "text";

        public ResponseFormat() {
        }

        public ResponseFormat(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public static ResponseFormat text() {
            return new ResponseFormat("text");
        }

        public static ResponseFormat jsonObject() {
            return new ResponseFormat("json_object");
        }
    }

    public DeepSeekChatRequest() {
    }

    public DeepSeekChatRequest(String model, List<DeepSeekMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<DeepSeekMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DeepSeekMessage> messages) {
        this.messages = messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }
}
