package com.gsms.gsms.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DeepSeek API 消息
 */
public class DeepSeekMessage {

    /**
     * 角色：system, user, assistant
     */
    @JsonProperty("role")
    private String role;

    /**
     * 消息内容
     */
    @JsonProperty("content")
    private String content;

    public DeepSeekMessage() {
    }

    public DeepSeekMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static DeepSeekMessage system(String content) {
        return new DeepSeekMessage("system", content);
    }

    public static DeepSeekMessage user(String content) {
        return new DeepSeekMessage("user", content);
    }

    public static DeepSeekMessage assistant(String content) {
        return new DeepSeekMessage("assistant", content);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
