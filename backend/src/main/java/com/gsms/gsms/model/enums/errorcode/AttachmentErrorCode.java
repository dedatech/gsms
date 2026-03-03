package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 附件模块错误码枚举 8xxx
 */
public enum AttachmentErrorCode implements ErrorCode {

    // ========== 业务验证错误（建议HTTP 400） ==========
    ATTACHMENT_NOT_FOUND(8001, "附件不存在"),
    ATTACHMENT_FILE_EMPTY(8002, "附件文件不能为空"),
    ATTACHMENT_FILE_SIZE_EXCEEDED(8003, "附件文件大小超过限制（最大10MB）"),
    ATTACHMENT_TARGET_INVALID(8004, "附件关联对象无效"),
    ATTACHMENT_TYPE_INVALID(8005, "附件类型无效"),

    // ========== 权限错误 ==========
    ATTACHMENT_NO_PERMISSION(8006, "您没有权限执行此操作"),

    // ========== 操作失败（建议HTTP 500） ==========
    ATTACHMENT_UPLOAD_FAILED(8901, "附件上传失败"),
    ATTACHMENT_DELETE_FAILED(8902, "附件删除失败"),
    ATTACHMENT_DOWNLOAD_FAILED(8903, "附件下载失败");

    private final int code;
    private final String message;

    AttachmentErrorCode(int code, String message) {
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
