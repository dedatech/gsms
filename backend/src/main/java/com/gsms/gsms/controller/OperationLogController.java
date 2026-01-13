package com.gsms.gsms.controller;

import com.gsms.gsms.dto.operationlog.OperationLogInfoResp;
import com.gsms.gsms.dto.operationlog.OperationLogQueryReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志管理", description = "操作日志相关接口")
@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public PageResult<OperationLogInfoResp> findByPage(@Valid OperationLogQueryReq queryReq) {
        return operationLogService.findByPage(queryReq);
    }

    /**
     * 根据ID查询操作日志详情
     */
    @Operation(summary = "根据ID查询操作日志详情")
    @GetMapping("/{id}")
    public Result<OperationLogInfoResp> getById(@PathVariable Long id) {
        OperationLogInfoResp log = operationLogService.getById(id);
        if (log == null) {
            return Result.error(404, "操作日志不存在");
        }
        return Result.success(log);
    }
}
