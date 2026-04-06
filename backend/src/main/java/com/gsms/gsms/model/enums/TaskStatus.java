package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 任务状态枚举
 *
 * 支持普通任务和缺陷的状态流转
 * 普通任务：TODO → IN_PROGRESS → DONE → CLOSED
 * 缺陷工作流：TODO → IN_PROGRESS → TESTING → DONE → CLOSED
 *            ↑                         ↓
 *            └─────── REOPENED ─────────┘
 */
public enum TaskStatus {
    TODO(1, "待处理"),
    IN_PROGRESS(2, "进行中"),
    TESTING(4, "待验证"),
    REOPENED(5, "重新打开"),
    DONE(3, "已完成"),
    CLOSED(6, "已关闭");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    TaskStatus(Integer code, String desc) {
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
