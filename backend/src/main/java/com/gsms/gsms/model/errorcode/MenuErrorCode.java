package com.gsms.gsms.model.enums.errorcode;

import com.gsms.gsms.infra.exception.ErrorCode;

/**
 * 菜单模块错误码枚举 14xxx
 * HTTP状态码建议：
 * - 14401-14408: 业务验证错误，建议HTTP 400
 * - 14405-14407: 操作失败，建议HTTP 500
 */
public enum MenuErrorCode implements ErrorCode {

    // ========== 业务验证错误（建议HTTP 400） ==========
    MENU_NOT_FOUND(14401, "菜单不存在"),
    PARENT_MENU_NOT_FOUND(14402, "父菜单不存在"),
    PARENT_MENU_CANNOT_BE_SELF(14403, "父菜单不能设置为自己"),
    MENU_HAS_CHILDREN(14404, "菜单存在子菜单，无法删除"),

    // ========== 操作失败（建议HTTP 500） ==========
    MENU_CREATE_FAILED(14405, "菜单创建失败"),
    MENU_UPDATE_FAILED(14406, "菜单更新失败"),
    MENU_DELETE_FAILED(14407, "菜单删除失败");

    private final int code;
    private final String message;

    MenuErrorCode(int code, String message) {
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
