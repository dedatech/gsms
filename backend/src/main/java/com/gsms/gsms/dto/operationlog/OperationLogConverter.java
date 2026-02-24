package com.gsms.gsms.dto.operationlog;

import com.gsms.gsms.model.entity.OperationLog;

import java.util.List;

/**
 * 操作日志对象转换器
 */
public class OperationLogConverter {

    /**
     * 操作日志实体转响应DTO
     */
    public static OperationLogInfoResp toOperationLogInfoResp(OperationLog log) {
        if (log == null) {
            return null;
        }
        OperationLogInfoResp resp = new OperationLogInfoResp();
        resp.setId(log.getId());
        resp.setUserId(log.getUserId());
        resp.setUsername(log.getUsername());
        resp.setOperationType(log.getOperationType());
        resp.setModule(log.getModule());
        resp.setBusinessType(log.getBusinessType());
        // businessId 现在是 String 类型
        resp.setBusinessId(log.getBusinessId() != null ? parseBusinessId(log.getBusinessId()) : null);
        resp.setOldValue(log.getOldValue());
        resp.setNewValue(log.getNewValue());
        // operationContent 字段已移除，可以从 newValue 中解析
        resp.setOperationContent(parseOperationContent(log.getNewValue()));
        resp.setIpAddress(log.getIpAddress());
        resp.setStatus(log.getStatus());
        resp.setErrorMessage(log.getErrorMessage());
        // operationTime 字段已移除，使用 createTime
        resp.setOperationTime(log.getCreateTime());
        return resp;
    }

    /**
     * 尝试解析 businessId 为 Long 类型
     */
    private static Long parseBusinessId(String businessId) {
        if (businessId == null || businessId.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(businessId);
        } catch (NumberFormatException e) {
            // 如果不是数字，返回 null
            return null;
        }
    }

    /**
     * 从 newValue 中解析 operationContent
     */
    private static String parseOperationContent(String newValue) {
        if (newValue == null || newValue.trim().isEmpty()) {
            return null;
        }
        // 简单解析 {"content":"xxx"} 格式
        if (newValue.contains("\"content\"")) {
            try {
                int start = newValue.indexOf("\"content\":\"") + 11;
                int end = newValue.lastIndexOf("\"");
                if (start > 11 && end > start) {
                    return newValue.substring(start, end);
                }
            } catch (Exception e) {
                // 解析失败，返回原始值
            }
        }
        return newValue;
    }

    /**
     * 批量转换
     */
    public static List<OperationLogInfoResp> toOperationLogInfoRespList(List<OperationLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return logs.stream()
                .map(OperationLogConverter::toOperationLogInfoResp)
                .collect(java.util.stream.Collectors.toList());
    }
}
