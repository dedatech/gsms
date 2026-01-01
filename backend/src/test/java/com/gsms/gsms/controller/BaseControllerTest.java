package com.gsms.gsms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Supplier;

/**
 * Controller测试基类
 * 提供统一的测试环境配置和工具方法
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseControllerTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    /**
     * 在指定用户上下文中执行操作（有返回值）
     * 用于在setUp中调用需要登录态的Service方法
     * 
     * @param userId 用户ID
     * @param action 需要执行的操作
     * @param <T> 返回值类型
     * @return 操作返回值
     */
    protected <T> T executeWithUserContext(Long userId, Supplier<T> action) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", userId);
        
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request)
        );
        
        try {
            return action.get();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
    
    /**
     * 在指定用户上下文中执行操作（无返回值）
     * 
     * @param userId 用户ID
     * @param action 需要执行的操作
     */
    protected void executeWithUserContext(Long userId, Runnable action) {
        executeWithUserContext(userId, () -> {
            action.run();
            return null;
        });
    }
}
