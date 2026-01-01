package com.gsms.gsms.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 分页查询请求基类
 * 所有需要分页的查询DTO应继承此类
 */
@Schema(description = "分页查询请求基类")
public class BasePageQuery {
    
    @Schema(description = "页码，从1开始", example = "1")
    private Integer pageNum;
    
    @Schema(description = "每页条数，最大不超过100", example = "10")
    private Integer pageSize;

  
    // Getters and Setters
    public Integer getPageNum() {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
