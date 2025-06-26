package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.health.HealthCheckConfig;
import com.muxin.gateway.refactory.route.RouteTarget;
import com.muxin.gateway.refactory.route.UniversalRequestContext;
import com.muxin.gateway.refactory.node.HttpEndpointAddress;

import java.time.Duration;
import java.util.*;

/**
 * 简单路由目标实现
 * 提供基本的路由目标功能，支持单个或多个目标地址
 *
 * @author muxin
 */
public class SimpleRouteTarget implements RouteTarget {
    
    private final String serviceName;
    private final List<EndpointAddress> endpoints;
    private final Protocol protocol;
    private final Map<String, Object> config;
    
    /**
     * 构造函数 - 单个目标地址
     */
    public SimpleRouteTarget(String targetUrl) {
        this("default-service", Arrays.asList(new HttpEndpointAddress(targetUrl)));
    }
    
    /**
     * 构造函数 - 指定服务名和单个目标地址
     */
    public SimpleRouteTarget(String serviceName, String targetUrl) {
        this(serviceName, Arrays.asList(new HttpEndpointAddress(targetUrl)));
    }
    
    /**
     * 构造函数 - 多个目标地址
     */
    public SimpleRouteTarget(String serviceName, List<EndpointAddress> endpoints) {
        this.serviceName = serviceName;
        this.endpoints = new ArrayList<>(endpoints);
        this.protocol = new Protocol.HttpProtocol();
        this.config = createDefaultConfig();
    }
    
    /**
     * 构造函数 - 完整配置
     */
    public SimpleRouteTarget(String serviceName, List<EndpointAddress> endpoints, 
                           Protocol protocol, Map<String, Object> config) {
        this.serviceName = serviceName;
        this.endpoints = new ArrayList<>(endpoints);
        this.protocol = protocol != null ? protocol : new Protocol.HttpProtocol();
        this.config = config != null ? new HashMap<>(config) : createDefaultConfig();
    }
    
    @Override
    public Protocol getTargetProtocol() {
        return protocol;
    }
    
    @Override
    public List<EndpointAddress> getTargetAddresses() {
        return new ArrayList<>(endpoints);
    }
    
    @Override
    public String getLoadBalanceStrategy() {
        return config.getOrDefault("loadBalance", "ROUND_ROBIN").toString();
    }
    
    @Override
    public Map<String, Object> getTargetConfig() {
        return new HashMap<>(config);
    }
    
    @Override
    public HealthCheckConfig getHealthCheckConfig() {
        return new SimpleHealthCheckConfig();
    }
    
    @Override
    public EndpointAddress selectTarget(UniversalRequestContext context) {
        if (endpoints.isEmpty()) {
            return null;
        }
        
        // 简单的轮询选择
        int index = Math.abs(context.hashCode()) % endpoints.size();
        return endpoints.get(index);
    }
    
    /**
     * 创建默认配置
     */
    private Map<String, Object> createDefaultConfig() {
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("service", serviceName);
        defaultConfig.put("timeout", 5000);
        defaultConfig.put("loadBalance", "ROUND_ROBIN");
        defaultConfig.put("retries", 3);
        return defaultConfig;
    }
    
    /**
     * 添加目标地址
     */
    public void addEndpoint(EndpointAddress endpoint) {
        if (endpoint != null) {
            this.endpoints.add(endpoint);
        }
    }
    
    /**
     * 移除目标地址
     */
    public void removeEndpoint(EndpointAddress endpoint) {
        this.endpoints.remove(endpoint);
    }
    
    /**
     * 获取服务名称
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * 检查是否有可用的目标地址
     */
    public boolean hasAvailableTargets() {
        return !endpoints.isEmpty();
    }
    
    @Override
    public String toString() {
        return "SimpleRouteTarget{" +
                "serviceName='" + serviceName + '\'' +
                ", endpoints=" + endpoints.size() +
                ", protocol=" + protocol.getName() +
                '}';
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
        public Duration getInterval() {
            return Duration.ofSeconds(30);
        }
        
        @Override
        public Duration getTimeout() {
            return Duration.ofSeconds(5);
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