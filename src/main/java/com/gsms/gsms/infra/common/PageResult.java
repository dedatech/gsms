package com.gsms.gsms.infra.common;

import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页结果封装，继承Result并扩展分页信息
 * @param <T> 数据类型
 */
@Schema(description = "分页结果")
public class PageResult<T> extends Result<List<T>> {
    
    @Schema(description = "总记录数")
    private Long total;
    
    @Schema(description = "当前页码")
    private Integer pageNum;
    
    @Schema(description = "每页条数")
    private Integer pageSize;
    
    @Schema(description = "总页数")
    private Integer totalPages;

    public PageResult() {
        super();
    }

    /**
     * 成功分页结果
     */
    public static <T> PageResult<T> success(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(records);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages(pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0);
        return result;
    }
    
    /**
     * 通过PageInfo创建分页结果
     */
    public static <T> PageResult<T> success(List<T> records, PageInfo<T> pageInfo) {
        return success(records, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }
    
    /**
     * 通过PageHelper查询结果创建分页结果（直接使用PageHelper的上下文）
     */
    public static <T> PageResult<T> success(List<T> records) {
        com.github.pagehelper.PageInfo<T> pageInfo = new PageInfo<>(records);
        return success(records, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    // Getters and Setters
    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
