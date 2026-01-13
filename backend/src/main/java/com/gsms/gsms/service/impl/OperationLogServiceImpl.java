package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.dto.operationlog.OperationLogConverter;
import com.gsms.gsms.dto.operationlog.OperationLogInfoResp;
import com.gsms.gsms.dto.operationlog.OperationLogQueryReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.model.entity.OperationLog;
import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationStatus;
import com.gsms.gsms.model.enums.OperationType;
import com.gsms.gsms.repository.OperationLogMapper;
import com.gsms.gsms.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logSuccess(Long userId, String username, OperationType operationType,
                           OperationModule module, String operationContent, HttpServletRequest request) {
        log(userId, username, operationType, module, operationContent, null,
            OperationStatus.SUCCESS, getIpAddress(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logFailure(Long userId, String username, OperationType operationType,
                           OperationModule module, String operationContent, String errorMessage,
                           HttpServletRequest request) {
        log(userId, username, operationType, module, operationContent, errorMessage,
            OperationStatus.FAILED, getIpAddress(request));
    }

    @Override
    public PageResult<OperationLogInfoResp> findByPage(OperationLogQueryReq queryReq) {
        logger.debug("分页查询操作日志: queryReq={}", queryReq);

        // 使用PageHelper进行分页
        PageHelper.startPage(queryReq.getPageNum(), queryReq.getPageSize());

        List<OperationLog> logs = operationLogMapper.findByCondition(
                queryReq.getUsername(),
                queryReq.getModule(),
                queryReq.getOperationType(),
                queryReq.getStatus(),
                queryReq.getStartTime(),
                queryReq.getEndTime()
        );

        PageInfo<OperationLog> pageInfo = new PageInfo<>(logs);
        List<OperationLogInfoResp> logList = OperationLogConverter.toOperationLogInfoRespList(logs);

        return PageResult.success(logList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    public OperationLogInfoResp getById(Long id) {
        logger.debug("根据ID查询操作日志: {}", id);
        OperationLog log = operationLogMapper.selectById(id);
        if (log == null) {
            return null;
        }
        return OperationLogConverter.toOperationLogInfoResp(log);
    }

    /**
     * 记录操作日志（内部方法）
     */
    private void log(Long userId, String username, OperationType operationType,
                     OperationModule module, String operationContent, String errorMessage,
                     OperationStatus status, String ipAddress) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setOperationType(operationType);
            log.setModule(module);
            log.setOperationContent(operationContent);
            log.setIpAddress(ipAddress);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setOperationTime(LocalDateTime.now());

            operationLogMapper.insert(log);

            logger.debug("操作日志记录成功: username={}, type={}, module={}, content={}",
                    username, operationType, module, operationContent);
        } catch (Exception e) {
            // 记录日志失败不影响主业务
            logger.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}
