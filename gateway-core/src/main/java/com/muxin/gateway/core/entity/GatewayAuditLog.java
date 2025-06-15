package com.muxin.gateway.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作审计日志实体类
 * 
 * @author muxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("gateway_audit_log")
public class GatewayAuditLog {
    
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作类型（CREATE, UPDATE, DELETE, ENABLE, DISABLE）
     */
    private String operationType;
    
    /**
     * 资源类型（ROUTE, USER, CONFIG）
     */
    private String resourceType;
    
    /**
     * 资源ID
     */
    private String resourceId;
    
    /**
     * 操作详情（JSON格式）
     */
    private String operationDetail;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * User-Agent
     */
    private String userAgent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
} 