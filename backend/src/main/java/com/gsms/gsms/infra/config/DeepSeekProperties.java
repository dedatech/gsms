package com.gsms.gsms.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 配置属性
 */
@Component
@ConfigurationProperties(prefix = "deepseek.api")
public class DeepSeekProperties {

    /**
     * DeepSeek API Key
     */
    private String key;

    /**
     * DeepSeek API 地址
     */
    private String url = "https://api.deepseek.com/v1/chat/completions";

    /**
     * 使用的模型
     */
    private String model = "deepseek-chat";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
