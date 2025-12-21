package com.gsms.gsms.common;

/**
 * 统一错误码定义
 */
public enum ErrorCode {
    SUCCESS(200, "成功"),
    ERROR(500, "系统内部错误"),
    PARAM_ERROR(400, "参数错误"),
    AUTH_FAILED(401, "认证失败"),
    ACCESS_DENIED(403, "权限不足"),
    NOT_FOUND(404, "资源不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}