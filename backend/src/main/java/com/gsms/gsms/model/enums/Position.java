package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 岗位枚举
 */
public enum Position {

    NOT_SET(0, "未设置"),
    FULL_STACK(1, "全栈开发"),
    BACKEND(2, "后端开发"),
    FRONTEND(3, "前端开发"),
    QA(4, "质量保证"),
    PRODUCT_OWNER(5, "需求方");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    Position(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @JsonValue  // Jackson 序列化为JSON时输出的值（枚举的 name，如 "FULL_STACK", "BACKEND"）
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 根据 code 获取枚举
     */
    public static Position fromCode(Integer code) {
        if (code == null) {
            return NOT_SET;
        }
        for (Position position : Position.values()) {
            if (position.code.equals(code)) {
                return position;
            }
        }
        return NOT_SET;
    }
}
