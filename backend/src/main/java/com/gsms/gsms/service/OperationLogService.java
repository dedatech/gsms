package com.gsms.gsms.service;

import com.gsms.gsms.dto.operationlog.OperationLogInfoResp;
import com.gsms.gsms.dto.operationlog.OperationLogQueryReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationStatus;
import com.gsms.gsms.model.enums.OperationType;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 记录操作日志（成功）
     * @param userId 操作人ID
     * @param username 操作人用户名
     * @param operationType 操作类型
     * @param module 操作模块
     * @param operationContent 操作内容描述
     * @param request HTTP请求（获取IP）
     */
    void logSuccess(Long userId, String username, OperationType operationType,
                   OperationModule module, String operationContent, HttpServletRequest request);

    /**
     * 记录操作日志（失败）
     * @param userId 操作人ID
     * @param username 操作人用户名
     * @param operationType 操作类型
     * @param module 操作模块
     * @param operationContent 操作内容描述
     * @param errorMessage 错误信息
     * @param request HTTP请求（获取IP）
     */
    void logFailure(Long userId, String username, OperationType operationType,
                   OperationModule module, String operationContent, String errorMessage,
                   HttpServletRequest request);

    /**
     * 分页查询操作日志
     * @param queryReq 查询请求
     * @return 分页结果
     */
    PageResult<OperationLogInfoResp> findByPage(OperationLogQueryReq queryReq);

    /**
     * 根据ID查询操作日志
     * @param id 日志ID
     * @return 操作日志响应
     */
    OperationLogInfoResp getById(Long id);
}
