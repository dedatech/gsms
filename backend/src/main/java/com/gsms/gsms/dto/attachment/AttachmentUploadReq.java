package com.gsms.gsms.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 附件上传请求
 */
@Schema(description = "附件上传请求")
public class AttachmentUploadReq {

    @Schema(description = "关联对象类型: task/requirement", required = true)
    @NotBlank(message = "关联对象类型不能为空")
    private String targetType;

    @Schema(description = "关联对象ID", required = true)
    @NotNull(message = "关联对象ID不能为空")
    private Long targetId;

    @Schema(description = "上传的文件", required = true)
    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
