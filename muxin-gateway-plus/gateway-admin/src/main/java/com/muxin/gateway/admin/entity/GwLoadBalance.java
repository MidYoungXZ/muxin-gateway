package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 负载均衡配置实体
 *
 * @author muxin
 */
@Data
@Table("gw_load_balance")
public class GwLoadBalance {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    /**
     * 路由ID
     */
    private Long routeId;
    
    /**
     * 负载均衡策略：ROUND_ROBIN/WEIGHTED/LEAST_CONN/IP_HASH/RANDOM
     */
    private String strategy;
    
    /**
     * 负载均衡配置（JSON格式）
     */
    @Column(typeHandler = com.mybatisflex.core.handler.JacksonTypeHandler.class)
    private LoadBalanceConfig config;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    private Boolean enabled;
    
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
     * 负载均衡配置内部类
     */
    @Data
    public static class LoadBalanceConfig {
        private HealthCheckConfig healthCheck;
        private Map<String, Object> strategyConfig;
        
        @Data
        public static class HealthCheckConfig {
            private Boolean enabled;
            private Integer interval;
            private Integer timeout;
            private String path;
            private java.util.List<Integer> expectedStatus;
        }
    }
} 