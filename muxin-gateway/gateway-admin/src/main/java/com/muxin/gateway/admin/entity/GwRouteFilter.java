package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 路由过滤器关联表实体
 *
 * @author muxin
 */
@Data
@Table("gw_route_filter")
public class GwRouteFilter {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private Long routeId;
    
    private Long filterId;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;
} 