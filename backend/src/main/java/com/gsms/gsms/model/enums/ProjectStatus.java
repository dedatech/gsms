package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 项目状态枚举
 */
public enum ProjectStatus {
    NOT_STARTED(1, "未开始"),
    IN_PROGRESS(2, "进行中"),
    SUSPENDED(3, "已挂起"),
    ARCHIVED(4, "已归档");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    ProjectStatus(Integer code, String desc) {
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
    public static ProjectStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的项目状态: " + code);
    }
}
