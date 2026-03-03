package com.gsms.gsms.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 附件重命名请求
 */
@Schema(description = "附件重命名请求")
public class AttachmentRenameReq {

    @Schema(description = "附件ID", required = true)
    @NotNull(message = "附件ID不能为空")
    private Long id;

    @Schema(description = "新的显示名称", required = true)
    @NotBlank(message = "显示名称不能为空")
    private String displayName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
