package com.muxin.gateway.admin.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 过滤器VO
 *
 * @author muxin
 */
@Data
public class FilterVO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 过滤器名称
     */
    private String filterName;
    
    /**
     * 过滤器类型
     */
    private String filterType;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 配置
     */
    private Map<String, Object> config;
    
    /**
     * 排序
     */
    private Integer order;
    
    /**
     * 是否系统内置
     */
    private Boolean isSystem;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 使用次数
     */
    private Long usageCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
} 