package com.gsms.gsms.infra.config;

import com.gsms.gsms.service.storage.LocalStorageProvider;
import com.gsms.gsms.service.storage.RustFSStorageProvider;
import com.gsms.gsms.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 存储配置类
 * 支持多种存储类型：local, rustfs
 */
@Configuration
public class StorageConfig {

    @Value("${attachment.storage.type:local}")
    private String storageType;

    /**
     * 根据配置获取存储服务实现
     */
    @Bean
    @Primary
    public StorageService storageService(
            LocalStorageProvider localStorageProvider,
            RustFSStorageProvider rustfsStorageProvider) {

        switch (storageType.toLowerCase()) {
            case "rustfs":
                return rustfsStorageProvider;
            case "local":
            default:
                return localStorageProvider;
        }
    }

    public String getStorageType() {
        return storageType;
    }
}
