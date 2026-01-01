package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 项目成员角色枚举
 */
public enum ProjectMemberRole {
    PROJECT_MANAGER(1, "项目经理"),
    MEMBER(2, "普通成员");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    ProjectMemberRole(Integer code, String desc) {
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
    public static ProjectMemberRole fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProjectMemberRole role : ProjectMemberRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的项目成员角色: " + code);
    }
}
