package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 角色类型枚举
 * SYSTEM: 系统预置角色（不可删除），如 SYS_ADMIN, EMPLOYEE
 * CUSTOM: 用户自定义角色（可删除）
 *
 * 注意：此枚举映射到 MySQL ENUM 类型（存储字符串），不是整数
 * 使用 @EnumValue 标记 value 字段，该字段的值等于枚举名称
 */
public enum RoleType {
    SYSTEM("SYSTEM", "系统角色"),
    CUSTOM("CUSTOM", "自定义角色");

    @EnumValue  // MyBatis-Plus 标记：使用此字段作为数据库存储值
    private final String value;  // 存储到数据库的值（"SYSTEM" 或 "CUSTOM"）
    private final String desc;

    RoleType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    @JsonValue  // Jackson 序列化为JSON时输出的值（枚举的 name，如 "SYSTEM", "CUSTOM"）
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 根据名称获取枚举
     */
    public static RoleType fromName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return RoleType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的角色类型: " + name + ", 有效值为: SYSTEM, CUSTOM");
        }
    }
}
