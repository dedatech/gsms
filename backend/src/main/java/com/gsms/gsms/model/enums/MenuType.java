package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 菜单类型枚举
 */
public enum MenuType {
    DIRECTORY(1, "目录"),
    MENU(2, "菜单"),
    BUTTON(3, "按钮");

    @EnumValue
    private final Integer code;
    private final String desc;

    MenuType(Integer code, String desc) {
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
    public static MenuType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MenuType type : MenuType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的菜单类型: " + code);
    }
}
