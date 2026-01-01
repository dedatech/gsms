package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 项目模块错误码枚举 3xxx
 * HTTP状态码建议：
 * - 3001-3006: 业务验证错误，建议HTTP 400
 * - 3901-3903: 操作失败，建议HTTP 500
 */
public enum ProjectErrorCode implements ErrorCode {
    
    // ========== 业务验证错误（建议HTTP 400） ==========
    PROJECT_NOT_FOUND(3001, "项目不存在"),         // 也可 HTTP 404
    PROJECT_NAME_EXISTS(3002, "项目名称已存在"),
    PROJECT_CODE_EXISTS(3003, "项目编码已存在"),
    PROJECT_STATUS_INVALID(3004, "项目状态无效"),
    PROJECT_MANAGER_INVALID(3005, "项目负责人无效"),
    PROJECT_DATE_INVALID(3006, "项目日期无效"),
    
    // ========== 操作失败（建议HTTP 500） ==========
    PROJECT_CREATE_FAILED(3901, "项目创建失败"),
    PROJECT_UPDATE_FAILED(3902, "项目更新失败"),
    PROJECT_DELETE_FAILED(3903, "项目删除失败");
    
    private final int code;
    private final String message;
    
    ProjectErrorCode(int code, String message) {
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
