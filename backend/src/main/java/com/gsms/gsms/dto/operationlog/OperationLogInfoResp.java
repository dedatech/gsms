package com.gsms.gsms.dto.operationlog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationStatus;
import com.gsms.gsms.model.enums.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 操作日志信息响应
 */
@Schema(description = "操作日志信息响应")
public class OperationLogInfoResp {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作人ID")
    private Long userId;

    @Schema(description = "操作人用户名")
    private String username;

    @Schema(description = "操作类型")
    private OperationType operationType;

    @Schema(description = "操作模块")
    private OperationModule module;

    @Schema(description = "操作内容描述")
    private String operationContent;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "操作状态")
    private OperationStatus status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public OperationModule getModule() {
        return module;
    }

    public void setModule(OperationModule module) {
        this.module = module;
    }

    public String getOperationContent() {
        return operationContent;
    }

    public void setOperationContent(String operationContent) {
        this.operationContent = operationContent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }
}
