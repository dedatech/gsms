package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 操作类型枚举
 */
public enum OperationType {
    CREATE(1, "CREATE", "创建"),
    UPDATE(2, "UPDATE", "更新"),
    DELETE(3, "DELETE", "删除"),
    ASSIGN(4, "ASSIGN", "分配"),
    REMOVE(5, "REMOVE", "移除"),
    LOGIN(6, "LOGIN", "登录"),
    LOGOUT(7, "LOGOUT", "登出"),
    QUERY(8, "QUERY", "查询");

    @EnumValue
    private final Integer code;

    private final String name;

    private final String description;

    OperationType(Integer code, String name, String description) {
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
    public static OperationType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationType type : OperationType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的操作类型: " + code);
    }
}
