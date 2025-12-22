package com.gsms.gsms.infra.exception;

/**
 * 通用错误码枚举
 * 错误码规则：
 * - 1xxx: 通用业务错误（建议HTTP 400）
 * - 14xx: 认证授权错误（建议HTTP 401/403）
 * - 15xx: 系统错误（建议HTTP 500）
 */
public enum CommonErrorCode implements ErrorCode {
    
    // ========== 通用业务错误 1xxx（建议HTTP 400） ==========
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "缺少必要参数"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),
    PARAM_INVALID(1004, "参数值无效"),
    
    // ========== 认证授权错误 14xx ==========
    UNAUTHORIZED(1401, "未授权，请先登录"),        // 建议HTTP 401
    TOKEN_EXPIRED(1402, "登录已过期，请重新登录"),   // 建议HTTP 401
    FORBIDDEN(1403, "无权限访问"),                 // 建议HTTP 403
    NOT_FOUND(1404, "资源不存在"),                 // 建议HTTP 404
    
    // ========== 系统错误 15xx（建议HTTP 500） ==========
    INTERNAL_ERROR(1500, "服务器内部错误"),
    DATABASE_ERROR(1501, "数据库操作异常"),
    SERVICE_UNAVAILABLE(1503, "服务暂时不可用"),
    
    // ========== 其他 ==========
    BUSINESS_ERROR(1999, "业务处理失败");           // 建议HTTP 400
    
    private final int code;
    private final String message;
    
    CommonErrorCode(int code, String message) {
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
