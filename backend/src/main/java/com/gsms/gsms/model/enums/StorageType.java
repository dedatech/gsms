package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 存储类型枚举
 */
public enum StorageType {
    LOCAL("local", "本地存储"),
    OSS("oss", "阿里云OSS"),
    COS("cos", "腾讯云COS"),
    MINIO("minio", "MinIO对象存储");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final String code;
    private final String desc;

    StorageType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue  // Jackson 序列化为JSON时输出的值
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static StorageType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (StorageType type : StorageType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的存储类型: " + code);
    }
}
