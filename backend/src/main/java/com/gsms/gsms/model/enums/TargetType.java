package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 关联对象类型枚举
 */
public enum TargetType {
    PROJECT("project", "项目"),
    TASK("task", "任务"),
    REQUIREMENT("requirement", "需求");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final String code;
    private final String desc;

    TargetType(String code, String desc) {
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
    public static TargetType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TargetType type : TargetType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的关联对象类型: " + code);
    }
}
