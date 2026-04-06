package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 缺陷严重程度枚举
 */
public enum DefectSeverity {
    TRIVIAL(1, "轻微"),
    MINOR(2, "次要"),
    MAJOR(3, "主要"),
    CRITICAL(4, "严重"),
    BLOCKER(5, "致命");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    DefectSeverity(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return this.name();
    }

    @JsonValue  // Jackson 序列化为JSON时输出的值（枚举的 name）
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 根据 code 获取枚举
     */
    public static DefectSeverity fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DefectSeverity severity : DefectSeverity.values()) {
            if (severity.code.equals(code)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("无效的严重程度: " + code);
    }
}
