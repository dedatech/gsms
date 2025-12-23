package com.gsms.gsms.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 任务类型枚举
 */
public enum TaskType {
    TASK(1, "任务"),
    REQUIREMENT(2, "需求"),
    BUG(3, "缺陷");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    TaskType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue  // Jackson 序列化为JSON时输出的值
    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static TaskType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskType type : TaskType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的任务类型: " + code);
    }
}
