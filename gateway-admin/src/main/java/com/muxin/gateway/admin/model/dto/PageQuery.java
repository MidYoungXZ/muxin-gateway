package com.muxin.gateway.admin.model.dto;

import lombok.Data;

/**
 * 分页查询基类
 *
 * @author muxin
 */
@Data
public class PageQuery {
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 排序字段
     */
    private String orderBy;
    
    /**
     * 排序方向
     */
    private String orderDirection = "ASC";
    
    /**
     * 游标（用于游标分页）
     */
    private String cursor;
} 