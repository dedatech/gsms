package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 菜单状态枚举
 */
public enum MenuStatus {
    ENABLED(1, "启用"),
    DISABLED(2, "禁用");

    @EnumValue
    private final Integer code;
    private final String desc;

    MenuStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 根据 code 获取枚举
     */
    public static MenuStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MenuStatus status : MenuStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的菜单状态: " + code);
    }
}
