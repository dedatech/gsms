package com.gsms.gsms.infra.utils;

import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationType;
import com.gsms.gsms.service.OperationLogService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志辅助工具类
 * 用于简化操作日志记录
 */
@Component
public class OperationLogHelper {

    private final OperationLogService operationLogService;

    public OperationLogHelper(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 记录成功操作
     * @param operationType 操作类型
     * @param module 操作模块
     * @param operationContent 操作内容描述
     */
    public void logSuccess(OperationType operationType, OperationModule module, String operationContent) {
        Long currentUserId = UserContext.getCurrentUserId();
        String currentUsername = UserContext.getCurrentUsername();
        HttpServletRequest request = getCurrentRequest();

        operationLogService.logSuccess(currentUserId, currentUsername, operationType, module,
                operationContent, request);
    }

    /**
     * 记录成功操作（带数据变更）
     * @param operationType 操作类型
     * @param module 操作模块
     * @param businessType 业务类型（如 USER, PROJECT, TASK 等）
     * @param businessId 业务ID（对应实体主键）
     * @param oldValue 变更前数据（会被序列化为JSON）
     * @param newValue 变更后数据（会被序列化为JSON）
     * @param operationContent 操作内容描述
     */
    public void logSuccessWithChanges(OperationType operationType, OperationModule module,
                                      String businessType, Long businessId,
                                      Object oldValue, Object newValue,
                                      String operationContent) {
        Long currentUserId = UserContext.getCurrentUserId();
        String currentUsername = UserContext.getCurrentUsername();
        HttpServletRequest request = getCurrentRequest();

        operationLogService.logSuccessWithChanges(currentUserId, currentUsername, operationType, module,
                businessType, businessId, oldValue, newValue, operationContent, request);
    }

    /**
     * 记录失败操作
     * @param operationType 操作类型
     * @param module 操作模块
     * @param operationContent 操作内容描述
     * @param errorMessage 错误信息
     */
    public void logFailure(OperationType operationType, OperationModule module, String operationContent,
                          String errorMessage) {
        Long currentUserId = UserContext.getCurrentUserId();
        String currentUsername = UserContext.getCurrentUsername();
        HttpServletRequest request = getCurrentRequest();

        operationLogService.logFailure(currentUserId, currentUsername, operationType, module,
                operationContent, errorMessage, request);
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
