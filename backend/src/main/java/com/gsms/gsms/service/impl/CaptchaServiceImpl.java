package com.gsms.gsms.service.impl;

import com.gsms.gsms.dto.auth.CaptchaResp;
import com.gsms.gsms.service.CaptchaService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 * 使用 Caffeine 缓存，5分钟自动过期
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {
    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    // 验证码缓存（UUID -> 验证码）
    private final Cache<String, String> captchaCache;

    // 验证码配置
    private static final int WIDTH = 120;           // 图片宽度
    private static final int HEIGHT = 40;           // 图片高度
    private static final int CODE_COUNT = 4;        // 验证码字符数
    private static final int LINE_COUNT = 3;        // 干扰线数量
    private static final int EXPIRE_MINUTES = 5;    // 过期时间（分钟）

    public CaptchaServiceImpl() {
        // 初始化 Caffeine 缓存
        this.captchaCache = Caffeine.newBuilder()
                .maximumSize(10_000)                          // 最多缓存1万条
                .expireAfterWrite(EXPIRE_MINUTES, TimeUnit.MINUTES)  // 5分钟过期
                .build();

        logger.info("验证码服务初始化完成：宽度={}px, 高度={}px, 字符数={}, 过期时间={}分钟",
                WIDTH, HEIGHT, CODE_COUNT, EXPIRE_MINUTES);
    }

    @Override
    public CaptchaResp generateCaptcha() {
        // 生成 UUID 作为验证码唯一标识
        String uuid = UUID.randomUUID().toString();

        // 使用 Hutool 生成线段干扰验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT, CODE_COUNT, LINE_COUNT);

        // 获取验证码文本（不区分大小写）
        String code = lineCaptcha.getCode().toLowerCase();

        // 存储到缓存
        captchaCache.put(uuid, code);

        logger.debug("生成验证码：uuid={}, code={}", uuid, code);

        // 生成 Base64 图片（添加 data URI 前缀）
        String imageBase64 = "data:image/png;base64," + lineCaptcha.getImageBase64();

        return new CaptchaResp(uuid, imageBase64);
    }

    @Override
    public boolean verifyCaptcha(String uuid, String code) {
        if (uuid == null || uuid.isEmpty()) {
            logger.warn("验证码验证失败：UUID为空");
            return false;
        }

        if (code == null || code.isEmpty()) {
            logger.warn("验证码验证失败：验证码为空");
            return false;
        }

        // 从缓存获取验证码
        String storedCode = captchaCache.getIfPresent(uuid);

        if (storedCode == null) {
            logger.warn("验证码验证失败：验证码不存在或已过期，uuid={}", uuid);
            return false;
        }

        // 不区分大小写比较
        boolean verified = storedCode.equalsIgnoreCase(code);

        if (verified) {
            logger.debug("验证码验证成功：uuid={}", uuid);
        } else {
            logger.warn("验证码验证失败：验证码错误，uuid={}, 输入={}, 正确={}",
                    uuid, code, storedCode);
        }

        // 验证后删除验证码（无论成功或失败）
        removeCaptcha(uuid);

        return verified;
    }

    @Override
    public void removeCaptcha(String uuid) {
        if (uuid != null) {
            captchaCache.invalidate(uuid);
            logger.debug("删除验证码：uuid={}", uuid);
        }
    }
}
