package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 分页响应VO
 *
 * @param <T> 数据类型
 * @author muxin
 */
@Data
@Builder
public class PageVO<T> {
    
    /**
     * 数据列表
     */
    private List<T> data;
    
    /**
     * 总数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 游标（用于游标分页）
     */
    private String cursor;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
} 