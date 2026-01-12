package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 权限模块错误码枚举 31xx
 * HTTP状态码建议：
 * - 3101-3106: 业务验证错误，建议HTTP 400
 * - 3901-3903: 操作失败，建议HTTP 500
 */
public enum PermissionErrorCode implements ErrorCode {

    // ========== 业务验证错误（建议HTTP 400） ==========
    PERMISSION_NOT_FOUND(3101, "权限不存在"),
    PERMISSION_CODE_EXISTS(3102, "权限编码已存在"),
    PERMISSION_NAME_EXISTS(3103, "权限名称已存在"),
    PERMISSION_CODE_INVALID(3104, "权限编码格式错误"),
    PERMISSION_IN_USE(3105, "权限正在使用中，无法删除"),
    PERMISSION_ALREADY_ASSIGNED(3106, "权限已分配给角色"),

    // ========== 操作失败（建议HTTP 500） ==========
    PERMISSION_CREATE_FAILED(3911, "权限创建失败"),
    PERMISSION_UPDATE_FAILED(3912, "权限更新失败"),
    PERMISSION_DELETE_FAILED(3913, "权限删除失败");

    private final int code;
    private final String message;

    PermissionErrorCode(int code, String message) {
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
