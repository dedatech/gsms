package com.gsms.gsms.controller;

import com.gsms.gsms.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World 控制器，用于测试框架是否正常工作
 */
@RestController
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    /**
     * Hello World 接口
     * @return 返回欢迎信息
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        logger.info("访问/hello接口");
        return Result.success("Welcome to GSMS - 工时管理系统");
    }

    /**
     * 受保护的接口，需要JWT Token才能访问
     * @return 返回受保护的信息
     */
    @GetMapping("/api/hello")
    public Result<String> protectedHello() {
        logger.info("访问/api/hello接口");
        return Result.success("这是受保护的接口，您已通过JWT认证");
    }
}