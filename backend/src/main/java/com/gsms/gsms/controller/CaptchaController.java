package com.gsms.gsms.controller;

import com.gsms.gsms.dto.auth.CaptchaResp;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/captcha")
@Tag(name = "验证码接口", description = "验证码相关的API接口")
public class CaptchaController {
    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 生成验证码
     */
    @GetMapping
    @Operation(summary = "生成验证码")
    public Result<CaptchaResp> generate() {
        logger.info("生成验证码");
        CaptchaResp captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }
}
