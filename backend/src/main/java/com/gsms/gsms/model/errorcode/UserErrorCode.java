package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 用户模块错误码枚举 2xxx
 * HTTP状态码建议：
 * - 2001-2006: 业务验证错误，建议HTTP 400
 * - 2901-2903: 操作失败，建议HTTP 500
 */
public enum UserErrorCode implements ErrorCode {
    
    // ========== 业务验证错误（建议HTTP 400） ==========
    USERNAME_EXISTS(2001, "用户名已存在"),
    USER_NOT_FOUND(2002, "用户不存在"),            // 也可 HTTP 404
    PASSWORD_ERROR(2003, "密码错误"),               // 也可 HTTP 401
    EMAIL_FORMAT_ERROR(2004, "邮箱格式错误"),
    PHONE_FORMAT_ERROR(2005, "手机号格式错误"),
    USER_DISABLED(2006, "用户已禁用"),             // 也可 HTTP 403
    DEFAULT_ROLE_NOT_FOUND(2007, "默认角色不存在"), // EMPLOYEE角色未配置
    
    // ========== 操作失败（建议HTTP 500） ==========
    USER_CREATE_FAILED(2901, "用户创建失败"),
    USER_UPDATE_FAILED(2902, "用户更新失败"),
    USER_DELETE_FAILED(2903, "用户删除失败");
    
    private final int code;
    private final String message;
    
    UserErrorCode(int code, String message) {
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
