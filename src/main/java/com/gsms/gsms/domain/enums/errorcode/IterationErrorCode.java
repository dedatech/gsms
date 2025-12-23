package com.gsms.gsms.domain.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

public enum IterationErrorCode implements ErrorCode {
    ITERATION_NOT_FOUND(5001, "迭代不存在"),
    ITERATION_CREATE_FAILED(5002, "迭代创建失败"),
    ITERATION_UPDATE_FAILED(5003, "迭代更新失败"),
    ITERATION_DELETE_FAILED(5004, "迭代删除失败"),
    ITERATION_HAS_TASKS(5005, "迭代下存在任务，无法删除");

    private final Integer code;
    private final String message;

    IterationErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
