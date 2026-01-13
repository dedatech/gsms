package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 项目类型枚举
 */
public enum ProjectType {
    /**
     * 常规型项目 - 简单项目，直接管理任务，无需迭代
     */
    SCHEDULE(1, "常规型项目"),

    /**
     * 中大型项目 - 复杂项目，支持迭代管理（如 Sprint）
     */
    LARGE_SCALE(2, "中大型项目");

    @EnumValue
    private final Integer code;

    private final String name;

    ProjectType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * Jackson 序列化为JSON时输出的值（枚举的 name）
     * 与项目中其他枚举保持一致
     */
    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 根据 code 获取枚举
     */
    public static ProjectType fromCode(Integer code) {
        if (code == null) {
            return SCHEDULE; // 默认常规型
        }
        for (ProjectType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown project type code: " + code);
    }
}
