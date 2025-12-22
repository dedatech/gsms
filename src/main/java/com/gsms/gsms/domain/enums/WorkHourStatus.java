package com.gsms.gsms.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 工时记录状态枚举
 */
public enum WorkHourStatus {
    SAVED(1, "已保存"),
    SUBMITTED(2, "已提交"),
    CONFIRMED(3, "已确认");

    private final Integer code;
    private final String desc;

    WorkHourStatus(Integer code, String desc) {
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
    public static WorkHourStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WorkHourStatus status : WorkHourStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的工时状态: " + code);
    }
}
