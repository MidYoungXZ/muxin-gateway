package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 路由模板实体
 *
 * @author muxin
 */
@Data
@Table("gw_route_template")
public class GwRouteTemplate {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 模板分类
     */
    private String category;
    
    /**
     * 模板配置（JSON格式）
     */
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private TemplateConfig config;
    
    /**
     * 模板变量定义（JSON格式）
     */
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private List<TemplateVariable> variables;
    
    /**
     * 是否系统内置：0-否，1-是
     */
    private Boolean isSystem;
    
    /**
     * 使用次数
     */
    private Integer usageCount;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    private Boolean enabled;
    
    /**
     * 是否删除：0-否，1-是
     */
    @Column(isLogicDelete = true)
    private Boolean deleted;
    
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
    
    /**
     * 模板配置内部类
     */
    @Data
    public static class TemplateConfig {
        private List<Map<String, Object>> predicates;
        private List<Map<String, Object>> filters;
        private Map<String, Object> metadata;
    }
    
    /**
     * 模板变量内部类
     */
    @Data
    public static class TemplateVariable {
        private String name;
        private String type; // string, number, boolean
        private Object defaultValue;
        private Boolean required;
        private String description;
    }
} 