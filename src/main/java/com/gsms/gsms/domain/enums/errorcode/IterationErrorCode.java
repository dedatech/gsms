package com.gsms.gsms.domain.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 迭代模块错误码枚举
 * 错误码段：7xxx
 */
public enum IterationErrorCode implements ErrorCode {
    ITERATION_NOT_FOUND(7001, "迭代不存在"),
    ITERATION_CREATE_FAILED(7002, "迭代创建失败"),
    ITERATION_UPDATE_FAILED(7003, "迭代更新失败"),
    ITERATION_DELETE_FAILED(7004, "迭代删除失败"),
    ITERATION_HAS_TASKS(7005, "迭代下存在任务，无法删除");

    private final int code;
    private final String message;

    IterationErrorCode(int code, String message) {
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