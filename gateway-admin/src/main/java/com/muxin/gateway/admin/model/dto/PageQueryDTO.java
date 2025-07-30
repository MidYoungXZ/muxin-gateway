package com.muxin.gateway.admin.model.dto;

import lombok.Data;

/**
 * 分页查询基类DTO
 *
 * @author muxin
 */
@Data
public class PageQueryDTO {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页条数
     */
    private Integer pageSize = 10;
} 