package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 配置字段VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class ConfigFieldVO {
    
    private String field;
    
    private String label;
    
    private String type;  // string/number/boolean/array/datetime
    
    private boolean required;
    
    private Object defaultValue;
    
    private String placeholder;
    
    private String description;
} 