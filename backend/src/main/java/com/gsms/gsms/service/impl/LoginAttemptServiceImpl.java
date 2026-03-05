package com.gsms.gsms.service.impl;

import com.gsms.gsms.service.LoginAttemptService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败记录服务实现类
 * 使用 Caffeine 缓存，自动过期
 *
 * 防暴力破解策略：
 * 1. 同一用户名连续失败3次，锁定10分钟
 * 2. 同一IP连续失败5次，限制10分钟
 */
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);

    // 用户登录失败记录（username -> 失败次数）
    private final Cache<String, Integer> userFailureCache;

    // IP登录失败记录（ip -> 失败次数）
    private final Cache<String, Integer> ipFailureCache;

    // 用户锁定时间记录（username -> 锁定开始时间戳）
    private final Cache<String, Long> userLockCache;

    // IP限制时间记录（ip -> 限制开始时间戳）
    private final Cache<String, Long> ipBlockCache;

    // 配置参数
    private static final int MAX_USER_FAILURES = 3;        // 用户最大失败次数
    private static final int MAX_IP_FAILURES = 5;          // IP最大失败次数
    private static final long LOCK_DURATION_MINUTES = 10;  // 锁定时长（分钟）
    private static final long LOCK_DURATION_SECONDS = LOCK_DURATION_MINUTES * 60;  // 锁定时长（秒）

    public LoginAttemptServiceImpl() {
        // 初始化 Caffeine 缓存
        // 失败次数缓存：10分钟过期
        this.userFailureCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        this.ipFailureCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        // 锁定时间缓存：10分钟过期
        this.userLockCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(LOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build();

        this.ipBlockCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(LOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build();

        logger.info("登录失败记录服务初始化完成：用户最大失败次数={}次, IP最大失败次数={}次, 锁定时长={}分钟",
                MAX_USER_FAILURES, MAX_IP_FAILURES, LOCK_DURATION_MINUTES);
    }

    @Override
    public void recordLoginFailure(String username, String ip) {
        // 记录用户失败次数
        if (username != null) {
            int userFailures = userFailureCache.get(username, k -> 0) + 1;
            userFailureCache.put(username, userFailures);

            // 检查是否达到锁定阈值
            if (userFailures >= MAX_USER_FAILURES) {
                long lockTime = System.currentTimeMillis();
                userLockCache.put(username, lockTime);
                logger.warn("用户登录失败次数过多，账户已临时锁定：username={}, 失败次数={}",
                        username, userFailures);
            } else {
                logger.info("记录用户登录失败：username={}, 失败次数={}/{}",
                        username, userFailures, MAX_USER_FAILURES);
            }
        }

        // 记录IP失败次数
        if (ip != null) {
            int ipFailures = ipFailureCache.get(ip, k -> 0) + 1;
            ipFailureCache.put(ip, ipFailures);

            // 检查是否达到限制阈值
            if (ipFailures >= MAX_IP_FAILURES) {
                long blockTime = System.currentTimeMillis();
                ipBlockCache.put(ip, blockTime);
                logger.warn("IP登录失败次数过多，IP已被临时限制：ip={}, 失败次数={}",
                        ip, ipFailures);
            } else {
                logger.info("记录IP登录失败：ip={}, 失败次数={}/{}",
                        ip, ipFailures, MAX_IP_FAILURES);
            }
        }
    }

    @Override
    public void recordLoginSuccess(String username, String ip) {
        // 登录成功，清除失败记录
        if (username != null) {
            userFailureCache.invalidate(username);
            userLockCache.invalidate(username);
            logger.debug("用户登录成功，清除失败记录：username={}", username);
        }

        if (ip != null) {
            ipFailureCache.invalidate(ip);
            ipBlockCache.invalidate(ip);
            logger.debug("IP登录成功，清除失败记录：ip={}", ip);
        }
    }

    @Override
    public boolean isLocked(String username) {
        if (username == null) {
            return false;
        }

        // 检查是否在锁定期内
        Long lockTime = userLockCache.getIfPresent(username);
        if (lockTime == null) {
            return false;
        }

        // 检查锁定期是否已过
        long elapsedSeconds = (System.currentTimeMillis() - lockTime) / 1000;
        if (elapsedSeconds >= LOCK_DURATION_SECONDS) {
            // 锁定期已过，清除锁定记录
            userLockCache.invalidate(username);
            userFailureCache.invalidate(username);
            logger.info("用户锁定期已过，自动解锁：username={}", username);
            return false;
        }

        logger.warn("用户仍在锁定期内：username={}, 剩余时间={}秒",
                username, LOCK_DURATION_SECONDS - elapsedSeconds);
        return true;
    }

    @Override
    public boolean isIpBlocked(String ip) {
        if (ip == null) {
            return false;
        }

        // 检查是否在限制期内
        Long blockTime = ipBlockCache.getIfPresent(ip);
        if (blockTime == null) {
            return false;
        }

        // 检查限制期是否已过
        long elapsedSeconds = (System.currentTimeMillis() - blockTime) / 1000;
        if (elapsedSeconds >= LOCK_DURATION_SECONDS) {
            // 限制期已过，清除限制记录
            ipBlockCache.invalidate(ip);
            ipFailureCache.invalidate(ip);
            logger.info("IP限制期已过，自动解除限制：ip={}", ip);
            return false;
        }

        logger.warn("IP仍在限制期内：ip={}, 剩余时间={}秒",
                ip, LOCK_DURATION_SECONDS - elapsedSeconds);
        return true;
    }

    @Override
    public long getRemainingLockTime(String username) {
        if (username == null) {
            return -1;
        }

        Long lockTime = userLockCache.getIfPresent(username);
        if (lockTime == null) {
            return -1;
        }

        long elapsedSeconds = (System.currentTimeMillis() - lockTime) / 1000;
        long remaining = LOCK_DURATION_SECONDS - elapsedSeconds;

        return remaining > 0 ? remaining : -1;
    }
}
