package com.gsms.gsms.dto.operationlog;

import com.gsms.gsms.dto.BasePageQuery;
import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationStatus;
import com.gsms.gsms.model.enums.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 操作日志查询请求
 */
@Schema(description = "操作日志查询请求")
public class OperationLogQueryReq extends BasePageQuery {

    @Schema(description = "操作人用户名", example = "admin")
    private String username;

    @Schema(description = "操作模块", example = "USER")
    private OperationModule module;

    @Schema(description = "操作类型", example = "CREATE")
    private OperationType operationType;

    @Schema(description = "操作状态", example = "SUCCESS")
    private OperationStatus status;

    @Schema(description = "开始时间（格式：yyyy-MM-dd HH:mm:ss）", example = "2026-01-01 00:00:00")
    private String startTime;

    @Schema(description = "结束时间（格式：yyyy-MM-dd HH:mm:ss）", example = "2026-01-31 23:59:59")
    private String endTime;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public OperationModule getModule() {
        return module;
    }

    public void setModule(OperationModule module) {
        this.module = module;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
