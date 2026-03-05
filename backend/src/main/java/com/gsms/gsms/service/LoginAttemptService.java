package com.gsms.gsms.service;

/**
 * 登录失败记录服务接口
 * 用于防止暴力破解攻击
 */
public interface LoginAttemptService {

    /**
     * 记录登录失败
     * @param username 用户名
     * @param ip IP地址
     */
    void recordLoginFailure(String username, String ip);

    /**
     * 记录登录成功
     * @param username 用户名
     * @param ip IP地址
     */
    void recordLoginSuccess(String username, String ip);

    /**
     * 检查是否被锁定
     * @param username 用户名
     * @return 是否被锁定
     */
    boolean isLocked(String username);

    /**
     * 检查IP是否被限制
     * @param ip IP地址
     * @return 是否被限制
     */
    boolean isIpBlocked(String ip);

    /**
     * 获取剩余锁定时间（秒）
     * @param username 用户名
     * @return 剩余锁定时间（秒），-1表示未锁定
     */
    long getRemainingLockTime(String username);
}
