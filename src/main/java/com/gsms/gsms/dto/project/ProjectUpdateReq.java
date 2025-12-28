package com.gsms.gsms.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 项目更新请求（继承公共字段 + 更新专属字段）
 */
@Schema(description = "项目更新请求")
public class ProjectUpdateReq extends ProjectBaseReq {
    
    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID", example = "1")
    private Long id;
    
    @Schema(description = "实际开始日期", example = "2025-01-05")
    private Date actualStartDate;
    
    @Schema(description = "实际结束日期", example = "2025-12-25")
    private Date actualEndDate;

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }
}
