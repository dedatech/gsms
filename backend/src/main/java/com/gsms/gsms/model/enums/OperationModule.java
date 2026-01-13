package com.gsms.gsms.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 操作模块枚举
 */
public enum OperationModule {
    USER(1, "USER", "用户管理"),
    ROLE(2, "ROLE", "角色管理"),
    PERMISSION(3, "PERMISSION", "权限管理"),
    PROJECT(4, "PROJECT", "项目管理"),
    TASK(5, "TASK", "任务管理"),
    WORK_HOUR(6, "WORK_HOUR", "工时管理"),
    DEPARTMENT(7, "DEPARTMENT", "部门管理"),
    ITERATION(8, "ITERATION", "迭代管理"),
    SYSTEM(9, "SYSTEM", "系统管理");

    @EnumValue
    private final Integer code;

    private final String name;

    private final String description;

    OperationModule(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 根据 code 获取枚举
     */
    public static OperationModule fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationModule module : OperationModule.values()) {
            if (module.code.equals(code)) {
                return module;
            }
        }
        throw new IllegalArgumentException("无效的操作模块: " + code);
    }
}
