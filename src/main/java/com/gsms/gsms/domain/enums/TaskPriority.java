package com.gsms.gsms.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 任务优先级枚举
 */
public enum TaskPriority {
    HIGH(1, "高"),
    MEDIUM(2, "中"),
    LOW(3, "低");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    TaskPriority(Integer code, String desc) {
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
    public static TaskPriority fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskPriority priority : TaskPriority.values()) {
            if (priority.code.equals(code)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("无效的任务优先级: " + code);
    }
}
