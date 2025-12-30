package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 工时模块错误码枚举 5xxx
 * HTTP状态码建议：
 * - 5001-5006: 业务验证错误，建议HTTP 400
 * - 5901-5903: 操作失败，建议HTTP 500
 */
public enum WorkHourErrorCode implements ErrorCode {
    
    // ========== 业务验证错误（建议HTTP 400） ==========
    WORKHOUR_NOT_FOUND(5001, "工时记录不存在"),     // 也可 HTTP 404
    WORKHOUR_DATE_INVALID(5002, "工时日期无效"),
    WORKHOUR_HOURS_EXCEED(5003, "工时超过24小时"),
    WORKHOUR_DUPLICATE(5004, "重复的工时记录"),
    WORKHOUR_STATUS_INVALID(5005, "工时状态无效"),
    WORKHOUR_PROJECT_INVALID(5006, "工时所属项目无效"),
    
    // ========== 操作失败（建议HTTP 500） ==========
    WORKHOUR_CREATE_FAILED(5901, "工时记录创建失败"),
    WORKHOUR_UPDATE_FAILED(5902, "工时记录更新失败"),
    WORKHOUR_DELETE_FAILED(5903, "工时记录删除失败");
    
    private final int code;
    private final String message;
    
    WorkHourErrorCode(int code, String message) {
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
