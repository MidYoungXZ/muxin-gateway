package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.FilterDefinition;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceDefinition;
import com.muxin.gateway.core.plus.route.predicate.PredicateDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 增强的路由配置类
 * 支持新的YAML配置结构（v2.0）
 * - 服务通过 service-ref 引用独立的服务定义
 * - 协议配置简化为枚举引用
 * - 支持配置继承机制
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDefinition {
    
    /**
     * 路由ID
     */
    private String id;
    
    /**
     * 路由名称
     */
    private String name;
    
    /**
     * 路由描述
     */
    private String description;
    
    /**
     * 路由优先级（数值越小优先级越高）
     */
    @Builder.Default
    private int order = 0;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;
    
    /**
     * 协议类型（枚举引用）
     * 从 ProtocolType 枚举中选择
     */
    private String protocol;
    
    /**
     * 服务引用（引用 services 中的服务ID）
     * 使用 service-ref 替代嵌套的 service 定义
     */
    private String serviceRef;
    
    /**
     * 断言配置列表（AND关系）
     */
    private List<PredicateDefinition> predicates;
    
    /**
     * 过滤器配置列表
     * 继承策略：APPEND（追加到全局过滤器）
     */
    private List<FilterDefinition> filters;
    
    /**
     * 负载均衡配置（路由级别）
     * 继承策略：OVERRIDE（覆盖全局）
     */
    private LoadBalanceDefinition loadBalance;
    
    /**
     * 超时配置
     * 继承策略：OVERRIDE（覆盖全局）
     */
    private TimeoutConfig timeouts;
    
    /**
     * 路由元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 获取负载均衡策略名称
     */
    public String getLoadBalanceStrategy() {
        return loadBalance != null ? loadBalance.getStrategy() : "ROUND_ROBIN";
    }
    
    /**
     * 是否配置了自定义负载均衡策略
     */
    public boolean hasCustomLoadBalance() {
        return loadBalance != null;
    }
    
    /**
     * 获取有效的负载均衡配置（考虑默认值）
     */
    public LoadBalanceDefinition getEffectiveLoadBalance() {
        if (loadBalance != null) {
            return loadBalance;
        }
        
        return LoadBalanceDefinition.builder()
                .strategy("ROUND_ROBIN")
                .build();
    }
    
    /**
     * 验证配置
     */
    public void validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("路由ID不能为空");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("路由名称不能为空");
        }
        
        if (serviceRef == null || serviceRef.trim().isEmpty()) {
            throw new IllegalArgumentException("服务引用（service-ref）不能为空");
        }
        
        if (predicates == null || predicates.isEmpty()) {
            throw new IllegalArgumentException("断言配置不能为空");
        }
        
        validateLoadBalanceConfig();
    }
    
    /**
     * 验证负载均衡配置
     */
    private void validateLoadBalanceConfig() {
        if (loadBalance != null) {
            if (loadBalance.getStrategy() == null || loadBalance.getStrategy().trim().isEmpty()) {
                throw new IllegalArgumentException("负载均衡策略名称不能为空");
            }
            
            if (!com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategyFactory
                    .isSupportedStrategy(loadBalance.getStrategy())) {
                throw new IllegalArgumentException("不支持的负载均衡策略: " + loadBalance.getStrategy());
            }
        }
    }
}