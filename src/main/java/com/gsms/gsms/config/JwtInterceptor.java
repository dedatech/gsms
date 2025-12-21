package com.gsms.gsms.config;

import com.gsms.gsms.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器，用于验证Token
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的Token
        String token = request.getHeader("Authorization");
        
        // 如果Token为空，返回401未授权
        if (token == null || token.isEmpty()) {
            logger.warn("请求未提供认证令牌: {} {}", request.getMethod(), request.getRequestURI());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未提供认证令牌\",\"data\":null}");
            return false;
        }
        
        // 验证Token是否有效
        if (!JwtUtil.validateToken(token)) {
            logger.warn("令牌无效或已过期: {} {}", request.getMethod(), request.getRequestURI());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"令牌无效或已过期\",\"data\":null}");
            return false;
        }
        
        // 将用户信息存储到请求属性中，供后续使用
        Long userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);
        
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        
        logger.info("用户认证成功: {} ({})", username, userId);
        return true;
    }
}