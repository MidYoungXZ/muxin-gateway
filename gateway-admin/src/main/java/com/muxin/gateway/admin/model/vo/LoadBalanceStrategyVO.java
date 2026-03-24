package com.muxin.gateway.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 负载均衡策略VO
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class LoadBalanceStrategyVO {
    
    private String code;
    
    private String name;
    
    private String description;
    
    private List<ConfigFieldVO> configFields;
}