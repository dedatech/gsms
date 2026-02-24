package com.gsms.gsms.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationStatus;
import com.gsms.gsms.model.enums.OperationType;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@TableName("sys_operation_log")
public class OperationLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作人用户名
     */
    private String username;

    /**
     * 操作类型（CREATE/UPDATE/DELETE/ASSIGN等）
     */
    @TableField("operation")
    private OperationType operationType;

    /**
     * 操作模块（USER/ROLE/PROJECT等）
     */
    private OperationModule module;

    /**
     * 业务类型（如 ROLE_PERMISSION, PROJECT_MEMBER 等）
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 业务ID（JSON格式或关键ID）
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 变更前数据（JSON格式）
     */
    @TableField("old_value")
    private String oldValue;

    /**
     * 变更后数据（JSON格式）
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * IP地址
     */
    @TableField("ip")
    private String ipAddress;

    /**
     * 请求URI
     */
    private String uri;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 执行时长（毫秒）
     */
    @TableField("execute_time")
    private Integer executeTime;

    /**
     * 操作状态（1:成功 0:失败）
     */
    private OperationStatus status;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMessage;

    /**
     * 创建时间（数据库自动填充）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @TableField("create_time")
    private LocalDateTime createTime;

    // Getter and Setter methods

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

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Integer executeTime) {
        this.executeTime = executeTime;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
