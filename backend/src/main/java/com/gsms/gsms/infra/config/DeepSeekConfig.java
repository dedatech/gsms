package com.gsms.gsms.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * DeepSeek 配置类
 */
@Configuration
public class DeepSeekConfig {

    /**
     * OkHttp 客户端 Bean
     */
    @Bean
    public OkHttpClient okHttpClient(DeepSeekProperties properties) {
        return new OkHttpClient.Builder()
                .connectTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * ObjectMapper Bean（用于 JSON 序列化）
     */
    @Bean
    public ObjectMapper deepseekObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 使用蛇形命名策略
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}
