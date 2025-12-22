package com.gsms.gsms.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    TODO(1, "待处理"),
    IN_PROGRESS(2, "进行中"),
    DONE(3, "已完成");

    private final Integer code;
    private final String desc;

    TaskStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static TaskStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatus status : TaskStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的任务状态: " + code);
    }
}
