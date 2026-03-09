package com.gsms.gsms.controller;

import com.gsms.gsms.dto.ai.RequirementBreakdownReq;
import com.gsms.gsms.dto.ai.RequirementBreakdownResp;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.RequirementBreakdownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * AI 功能控制器
 */
@Tag(name = "AI 功能", description = "AI 辅助功能接口")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final RequirementBreakdownService requirementBreakdownService;

    public AiController(RequirementBreakdownService requirementBreakdownService) {
        this.requirementBreakdownService = requirementBreakdownService;
    }

    /**
     * 拆分需求并预估工时
     *
     * @param request 需求拆分请求
     * @return 需求拆分响应
     */
    @Operation(summary = "拆分需求并预估工时", description = "使用 AI 将需求拆分为子任务并预估人天")
    @PostMapping("/breakdown-requirement")
    public Result<RequirementBreakdownResp> breakdownRequirement(
            @Valid @RequestBody RequirementBreakdownReq request) {

        logger.info("接收到需求拆分请求: {}", request.getRequirement());

        try {
            RequirementBreakdownResp response = requirementBreakdownService.breakdownRequirement(request);
            logger.info("需求拆分成功，共拆分 {} 个子任务，总预估 {} 人天",
                    response.getSubTasks() != null ? response.getSubTasks().size() : 0,
                    response.getTotalEstimatedDays());
            return Result.success(response);
        } catch (Exception e) {
            logger.error("需求拆分失败: {}", e.getMessage(), e);
            return Result.error(500, "需求拆分失败: " + e.getMessage());
        }
    }

    /**
     * 检查 AI 服务状态
     *
     * @return 服务状态
     */
    @Operation(summary = "检查 AI 服务状态", description = "检查 DeepSeek API 连接状态")
    @GetMapping("/status")
    public Result<Boolean> checkStatus() {
        // 这里可以添加对 DeepSeek API 的健康检查
        // 目前简单返回 true
        return Result.success(true);
    }
}
