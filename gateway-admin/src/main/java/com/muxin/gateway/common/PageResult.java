package com.muxin.gateway.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果
 *
 * @author muxin
 */
@Data
public class PageResult<T> {
    
    private Long total;
    
    private List<T> records;
    
    private Integer pageNum;
    
    private Integer pageSize;
    
    public static <T> PageResult<T> of(Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }
} 