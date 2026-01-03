package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IterationStatus {
    NOT_STARTED(1, "未开始"),
    IN_PROGRESS(2, "进行中"),
    COMPLETED(3, "已完成");

    @EnumValue
    private final Integer code;
    private final String desc;

    IterationStatus(Integer code, String desc) {
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

    public static IterationStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (IterationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的迭代状态: " + code);
    }
}
