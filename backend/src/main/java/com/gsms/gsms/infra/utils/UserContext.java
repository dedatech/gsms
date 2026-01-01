package com.gsms.gsms.infra.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类 - 获取当前登录用户信息
 */
public class UserContext {
    
    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }
    
    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        Object username = request.getAttribute("username");
        return username != null ? (String) username : null;
    }
    
    /**
     * 获取当前请求对象
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
