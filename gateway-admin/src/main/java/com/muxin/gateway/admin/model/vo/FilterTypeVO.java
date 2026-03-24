package com.muxin.gateway.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 过滤器类型VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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

    /**
     * 配置字段定义
     */
    private List<ConfigFieldVO> configFields;

    public FilterTypeVO(String value, String label, String description, Object configTemplate) {
        this.value = value;
        this.label = label;
        this.description = description;
        this.configTemplate = configTemplate;
    }
} 