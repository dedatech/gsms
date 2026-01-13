package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 操作状态枚举
 */
public enum OperationStatus {
    SUCCESS(1, "SUCCESS", "成功"),
    FAILED(2, "FAILED", "失败");

    @EnumValue
    private final Integer code;

    private final String name;

    private final String description;

    OperationStatus(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 根据 code 获取枚举
     */
    public static OperationStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationStatus status : OperationStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的操作状态: " + code);
    }
}
