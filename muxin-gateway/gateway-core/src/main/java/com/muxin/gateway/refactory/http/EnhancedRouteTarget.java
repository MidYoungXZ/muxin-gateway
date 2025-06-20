package com.muxin.gateway.refactory.http;

import com.muxin.gateway.refactory.*;

import java.util.*;

/**
 * 增强的路由目标实现，支持多地址和负载均衡
 *
 * @author muxin
 */
public class EnhancedRouteTarget implements RouteTarget {
    
    private final Protocol targetProtocol;
    private final List<EndpointAddress> targetAddresses;
    private final LoadBalanceStrategy loadBalanceStrategy;
    private final Map<String, Object> targetConfig;
    private final HealthCheckConfig healthCheckConfig;
    
    public EnhancedRouteTarget(List<String> targetUris, LoadBalanceStrategy loadBalanceStrategy) {
        this.targetProtocol = new HttpProtocol();
        this.targetAddresses = new ArrayList<>();
        
        // 创建端点地址
        for (String uri : targetUris) {
            this.targetAddresses.add(new HttpEndpointAddress(uri));
        }
        
        this.loadBalanceStrategy = loadBalanceStrategy;
        this.targetConfig = new HashMap<>();
        this.targetConfig.put("uris", targetUris);
        this.targetConfig.put("loadBalanceStrategy", loadBalanceStrategy.getName());
        
        this.healthCheckConfig = new SimpleHealthCheckConfig();
    }
    
    @Override
    public Protocol getTargetProtocol() {
        return targetProtocol;
    }
    
    @Override
    public List<EndpointAddress> getTargetAddresses() {
        return new ArrayList<>(targetAddresses);
    }
    
    @Override
    public String getLoadBalanceStrategy() {
        return loadBalanceStrategy.getName();
    }
    
    @Override
    public Map<String, Object> getTargetConfig() {
        return new HashMap<>(targetConfig);
    }
    
    @Override
    public HealthCheckConfig getHealthCheckConfig() {
        return healthCheckConfig;
    }
    
    @Override
    public EndpointAddress selectTarget(UniversalRequestContext context) {
        return loadBalanceStrategy.select(targetAddresses, context);
    }
    
    /**
     * 更新地址健康状态
     */
    public void updateHealthStatus(EndpointAddress address, boolean isHealthy) {
        loadBalanceStrategy.updateHealthStatus(address, isHealthy);
    }
    
    /**
     * 获取负载均衡策略
     */
    public LoadBalanceStrategy getLoadBalancer() {
        return loadBalanceStrategy;
    }
    
    /**
     * 简单健康检查配置实现
     */
    private static class SimpleHealthCheckConfig implements HealthCheckConfig {
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public java.time.Duration getInterval() {
            return java.time.Duration.ofSeconds(30);
        }
        
        @Override
        public java.time.Duration getTimeout() {
            return java.time.Duration.ofSeconds(5);
        }
        
        @Override
        public String getPath() {
            return "/health";
        }
        
        @Override
        public List<Integer> getExpectedStatusCodes() {
            return Arrays.asList(200, 204);
        }
        
        @Override
        public int getFailureThreshold() {
            return 3;
        }
        
        @Override
        public int getSuccessThreshold() {
            return 2;
        }
    }
} 