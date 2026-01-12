package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 角色模块错误码枚举 3xxx
 * HTTP状态码建议：
 * - 3001-3006: 业务验证错误，建议HTTP 400
 * - 3901-3903: 操作失败，建议HTTP 500
 */
public enum RoleErrorCode implements ErrorCode {

    // ========== 业务验证错误（建议HTTP 400） ==========
    ROLE_NOT_FOUND(3001, "角色不存在"),
    ROLE_CODE_EXISTS(3002, "角色编码已存在"),
    ROLE_NAME_EXISTS(3003, "角色名称已存在"),
    ROLE_CODE_INVALID(3004, "角色编码格式错误"),
    ROLE_IN_USE(3005, "角色正在使用中，无法删除"),
    ROLE_LEVEL_INVALID(3006, "角色级别无效"),

    // ========== 操作失败（建议HTTP 500） ==========
    ROLE_CREATE_FAILED(3901, "角色创建失败"),
    ROLE_UPDATE_FAILED(3902, "角色更新失败"),
    ROLE_DELETE_FAILED(3903, "角色删除失败");

    private final int code;
    private final String message;

    RoleErrorCode(int code, String message) {
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
