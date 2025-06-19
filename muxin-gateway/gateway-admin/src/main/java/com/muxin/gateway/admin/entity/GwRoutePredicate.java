package com.muxin.gateway.admin.entity;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 路由断言关联表实体
 *
 * @author muxin
 */
@Data
@Table("gw_route_predicate")
public class GwRoutePredicate {
    
    @Id(keyType = KeyType.Auto)
    private Long id;
    
    private Long routeId;
    
    private Long predicateId;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;
} 