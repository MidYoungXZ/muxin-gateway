package com.muxin.gateway.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路由简要信息VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSimpleVO {
    
    private Long id;

    private String routeId;

    private String routeName;

    private Boolean enabled;
}