package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 任务模块错误码枚举 4xxx
 * HTTP状态码建议：
 * - 4001-4006: 业务验证错误，建议HTTP 400
 * - 4901-4903: 操作失败，建议HTTP 500
 */
public enum TaskErrorCode implements ErrorCode {
    
    // ========== 业务验证错误（建议HTTP 400） ==========
    TASK_NOT_FOUND(4001, "任务不存在"),            // 也可 HTTP 404
    TASK_TITLE_EMPTY(4002, "任务标题不能为空"),
    TASK_ASSIGNEE_INVALID(4003, "任务负责人无效"),
    TASK_STATUS_INVALID(4004, "任务状态无效"),
    TASK_PRIORITY_INVALID(4005, "任务优先级无效"),
    TASK_PROJECT_INVALID(4006, "任务所属项目无效"),
    
    // ========== 操作失败（建议HTTP 500） ==========
    TASK_CREATE_FAILED(4901, "任务创建失败"),
    TASK_UPDATE_FAILED(4902, "任务更新失败"),
    TASK_DELETE_FAILED(4903, "任务删除失败");
    
    private final int code;
    private final String message;
    
    TaskErrorCode(int code, String message) {
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
