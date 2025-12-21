package com.gsms.gsms.config;

import com.gsms.gsms.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(HttpServletRequest request, Exception e) {
        logger.error("请求地址：{}，发生异常：", request.getRequestURL(), e);
        return Result.error("系统内部错误：" + e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        logger.warn("请求地址：{}，参数校验异常：{}", request.getRequestURL(), e.getMessage());
        return Result.error("参数错误：" + e.getMessage());
    }
}