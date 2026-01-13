package com.gsms.gsms.service.impl;

import com.gsms.gsms.infra.utils.OperationLogHelper;
import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationType;
import org.springframework.stereotype.Component;

/**
 * 可记录日志的Service支持类
 * 提供统一的日志记录方法
 */
@Component
public class LoggableServiceSupport {

    private final OperationLogHelper operationLogHelper;

    public LoggableServiceSupport(OperationLogHelper operationLogHelper) {
        this.operationLogHelper = operationLogHelper;
    }

    /**
     * 记录用户创建操作
     */
    public void logUserCreate(String username, String nickname, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.CREATE, OperationModule.USER,
                    String.format("创建用户: %s (%s)", username, nickname));
        } else {
            operationLogHelper.logFailure(OperationType.CREATE, OperationModule.USER,
                    String.format("创建用户: %s", username), errorMessage);
        }
    }

    /**
     * 记录用户更新操作
     */
    public void logUserUpdate(Long userId, String username, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.UPDATE, OperationModule.USER,
                    String.format("更新用户: ID=%d, 用户名=%s", userId, username));
        } else {
            operationLogHelper.logFailure(OperationType.UPDATE, OperationModule.USER,
                    String.format("更新用户: ID=%d", userId), errorMessage);
        }
    }

    /**
     * 记录用户删除操作
     */
    public void logUserDelete(Long userId, String username, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.DELETE, OperationModule.USER,
                    String.format("删除用户: %s (%s)", username, userId));
        } else {
            operationLogHelper.logFailure(OperationType.DELETE, OperationModule.USER,
                    String.format("删除用户: ID=%d", userId), errorMessage);
        }
    }

    /**
     * 记录用户角色分配操作
     */
    public void logUserRoleAssign(Long userId, String username, int roleCount, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.ASSIGN, OperationModule.USER,
                    String.format("为用户 %s 分配 %d 个角色", username, roleCount));
        } else {
            operationLogHelper.logFailure(OperationType.ASSIGN, OperationModule.USER,
                    String.format("为用户 ID=%d 分配角色", userId), errorMessage);
        }
    }

    /**
     * 记录角色创建操作
     */
    public void logRoleCreate(String roleName, String roleCode, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.CREATE, OperationModule.ROLE,
                    String.format("创建角色: %s (%s)", roleName, roleCode));
        } else {
            operationLogHelper.logFailure(OperationType.CREATE, OperationModule.ROLE,
                    String.format("创建角色: %s", roleName), errorMessage);
        }
    }

    /**
     * 记录角色更新操作
     */
    public void logRoleUpdate(Long roleId, String roleName, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.UPDATE, OperationModule.ROLE,
                    String.format("更新角色: %s (ID=%d)", roleName, roleId));
        } else {
            operationLogHelper.logFailure(OperationType.UPDATE, OperationModule.ROLE,
                    String.format("更新角色: ID=%d", roleId), errorMessage);
        }
    }

    /**
     * 记录角色删除操作
     */
    public void logRoleDelete(Long roleId, String roleName, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.DELETE, OperationModule.ROLE,
                    String.format("删除角色: %s (%s)", roleName, roleId));
        } else {
            operationLogHelper.logFailure(OperationType.DELETE, OperationModule.ROLE,
                    String.format("删除角色: ID=%d", roleId), errorMessage);
        }
    }

    /**
     * 记录角色权限分配操作
     */
    public void logRolePermissionAssign(Long roleId, String roleName, int permissionCount, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.ASSIGN, OperationModule.ROLE,
                    String.format("为角色 %s 分配 %d 个权限", roleName, permissionCount));
        } else {
            operationLogHelper.logFailure(OperationType.ASSIGN, OperationModule.ROLE,
                    String.format("为角色 %s (ID=%d) 分配权限", roleName, roleId), errorMessage);
        }
    }

    /**
     * 记录权限创建操作
     */
    public void logPermissionCreate(String permissionName, String permissionCode, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.CREATE, OperationModule.PERMISSION,
                    String.format("创建权限: %s (%s)", permissionName, permissionCode));
        } else {
            operationLogHelper.logFailure(OperationType.CREATE, OperationModule.PERMISSION,
                    String.format("创建权限: %s", permissionName), errorMessage);
        }
    }

    /**
     * 记录权限更新操作
     */
    public void logPermissionUpdate(Long permissionId, String permissionName, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.UPDATE, OperationModule.PERMISSION,
                    String.format("更新权限: %s (ID=%d)", permissionName, permissionId));
        } else {
            operationLogHelper.logFailure(OperationType.UPDATE, OperationModule.PERMISSION,
                    String.format("更新权限: ID=%d", permissionId), errorMessage);
        }
    }

    /**
     * 记录权限删除操作
     */
    public void logPermissionDelete(Long permissionId, String permissionName, boolean success, String errorMessage) {
        if (success) {
            operationLogHelper.logSuccess(OperationType.DELETE, OperationModule.PERMISSION,
                    String.format("删除权限: %s (%s)", permissionName, permissionId));
        } else {
            operationLogHelper.logFailure(OperationType.DELETE, OperationModule.PERMISSION,
                    String.format("删除权限: ID=%d", permissionId), errorMessage);
        }
    }
}
