package com.gsms.gsms.service;

import com.gsms.gsms.dto.auth.CaptchaResp;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成验证码
     * @return 验证码响应（包含UUID和Base64图片）
     */
    CaptchaResp generateCaptcha();

    /**
     * 验证验证码
     * @param uuid 验证码UUID
     * @param code 用户输入的验证码
     * @return 验证是否通过
     */
    boolean verifyCaptcha(String uuid, String code);

    /**
     * 删除验证码（验证后调用）
     * @param uuid 验证码UUID
     */
    void removeCaptcha(String uuid);
}
