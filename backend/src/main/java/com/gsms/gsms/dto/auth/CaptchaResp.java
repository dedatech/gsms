package com.gsms.gsms.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码响应
 */
@Schema(description = "验证码响应")
public class CaptchaResp {

    @Schema(description = "验证码UUID", example = "uuid-xxx-xxx")
    private String uuid;

    @Schema(description = "验证码图片（Base64）", example = "data:image/png;base64,iVBORw0KGgo...")
    private String image;

    public CaptchaResp() {
    }

    public CaptchaResp(String uuid, String image) {
        this.uuid = uuid;
        this.image = image;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
