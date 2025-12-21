package com.gsms.gsms.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 */
public class LogUtil {
    
    /**
     * 获取Logger实例
     * @param clazz 类
     * @return Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * 记录调试日志
     * @param clazz 类
     * @param message 消息
     * @param params 参数
     */
    public static void debug(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).debug(message, params);
    }
    
    /**
     * 记录信息日志
     * @param clazz 类
     * @param message 消息
     * @param params 参数
     */
    public static void info(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).info(message, params);
    }
    
    /**
     * 记录警告日志
     * @param clazz 类
     * @param message 消息
     * @param params 参数
     */
    public static void warn(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).warn(message, params);
    }
    
    /**
     * 记录错误日志
     * @param clazz 类
     * @param message 消息
     * @param params 参数
     */
    public static void error(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).error(message, params);
    }
    
    /**
     * 记录错误日志
     * @param clazz 类
     * @param message 消息
     * @param throwable 异常
     */
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).error(message, throwable);
    }
}