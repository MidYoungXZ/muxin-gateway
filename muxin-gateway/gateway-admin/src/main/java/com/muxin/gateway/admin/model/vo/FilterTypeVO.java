package com.muxin.gateway.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过滤器类型VO
 *
 * @author muxin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterTypeVO {
    
    /**
     * 类型值
     */
    private String value;
    
    /**
     * 类型名称
     */
    private String label;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 配置模板
     */
    private Object configTemplate;
} 