package com.muxin.gateway.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 路由配置实体类
 * 
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("gateway_route")
public class GatewayRoute {
    
    /**
     * 路由ID
     */
    @TableId
    private String id;
    
    /**
     * 目标URI
     */
    private String uri;
    
    /**
     * 断言配置（JSON格式）
     */
    private String predicates;
    
    /**
     * 过滤器配置（JSON格式）
     */
    private String filters;
    
    /**
     * 元数据（JSON格式）
     */
    private String metadata;
    
    /**
     * 排序号
     */
    @TableField("order_num")
    private Integer orderNum;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 版本号，用于变更检测
     */
    private Integer version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 