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
        resp.setOperationContent(log.getOperationContent());
        resp.setIpAddress(log.getIpAddress());
        resp.setStatus(log.getStatus());
        resp.setErrorMessage(log.getErrorMessage());
        resp.setOperationTime(log.getOperationTime());
        return resp;
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
