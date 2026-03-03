package com.gsms.gsms.infra.config;

import com.gsms.gsms.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 存储配置类
 */
@Configuration
public class StorageConfig {

    @Value("${attachment.storage.type:local}")
    private String storageType;

    public String getStorageType() {
        return storageType;
    }
}
