package com.gsms.gsms.domain.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 部门模块错误码枚举
 * 错误码段：6xxx
 */
public enum DepartmentErrorCode implements ErrorCode {
    DEPARTMENT_NOT_FOUND(6001, "部门不存在"),
    DEPARTMENT_CREATE_FAILED(6002, "部门创建失败"),
    DEPARTMENT_UPDATE_FAILED(6003, "部门更新失败"),
    DEPARTMENT_DELETE_FAILED(6004, "部门删除失败"),
    DEPARTMENT_HAS_CHILDREN(6005, "部门下存在子部门，无法删除"),
    DEPARTMENT_HAS_USERS(6006, "部门下存在用户，无法删除");

    private final int code;
    private final String message;

    DepartmentErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}