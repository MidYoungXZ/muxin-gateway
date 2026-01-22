package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 断言类型VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class PredicateTypeVO {
    
    private String type;
    
    private String name;
    
    private String description;
    
    private List<ConfigFieldVO> configFields;  // 配置字段定义
} 