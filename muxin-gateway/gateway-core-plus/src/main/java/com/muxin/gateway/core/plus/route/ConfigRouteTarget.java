package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CONFIG类型路由目标实现
 * 使用静态配置的地址列表
 *
 * @author muxin
 */
@Slf4j
public class ConfigRouteTarget implements RouteTarget {
    
    // ========== 服务标识信息 ==========
    private final String serviceId;
    private final String serviceName;
    private final ServiceType serviceType = ServiceType.CONFIG;
    
    // ========== 协议和地址 ==========
    private final Protocol supportProtocol;
    private final List<EndpointAddress> addresses;
    
    // ========== 负载均衡 ==========
    private final LoadBalanceStrategy loadBalanceStrategy;
    private final Map<String, Object> config;
    
    public ConfigRouteTarget(String serviceId,
                           String serviceName,
                           Protocol supportProtocol,
                           List<EndpointAddress> addresses,
                           LoadBalanceStrategy loadBalanceStrategy,
                           Map<String, Object> config) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId不能为空");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName不能为空");
        this.supportProtocol = Objects.requireNonNull(supportProtocol, "supportProtocol不能为空");
        this.addresses = Objects.requireNonNull(addresses, "addresses不能为空");
        this.loadBalanceStrategy = Objects.requireNonNull(loadBalanceStrategy, "loadBalanceStrategy不能为空");
        this.config = config;
        
        // 验证地址列表
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置至少一个地址");
        }
        
        log.info("创建CONFIG路由目标: {} ({}), 地址数量: {}, 负载均衡: {}", 
                serviceName, serviceId, addresses.size(), loadBalanceStrategy.getStrategyName());
    }

    @Override
    public RouteTargetDefinition routeTargetDefinition() {
        return null;
    }

    @Override
    public Protocol supportProtocol() {
        return supportProtocol;
    }
    
    @Override
    public List<EndpointAddress> getTargetAddresses() {
        return addresses;
    }
    
    @Override
    public LoadBalanceStrategy loadBalanceStrategy() {
        return loadBalanceStrategy;
    }
    
    @Override
    public Map<String, Object> getTargetConfig() {
        return config;
    }
    
    @Override
    public EndpointAddress selectTarget(RequestContext context) {
        if (addresses.isEmpty()) {
            throw new IllegalStateException("没有可用的地址");
        }
        
        try {
            // 使用负载均衡策略选择地址
            EndpointAddress selected = loadBalanceStrategy.select(addresses, context);
            
            log.debug("CONFIG路由目标选择地址: {} -> {} (策略: {})", 
                    serviceName, selected.toUri(), loadBalanceStrategy.getStrategyName());
            
            return selected;
            
        } catch (Exception e) {
            log.error("CONFIG路由目标选择地址失败: {}", serviceName, e);
            // 降级：返回第一个地址
            EndpointAddress fallback = addresses.get(0);
            log.warn("使用降级地址: {}", fallback.toUri());
            return fallback;
        }
    }
    
    // ========== Getter方法 ==========
    
    public String getServiceId() {
        return serviceId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    /**
     * 获取地址数量
     */
    public int getAddressCount() {
        return addresses.size();
    }
    
    /**
     * 检查是否包含指定地址
     */
    public boolean containsAddress(EndpointAddress address) {
        return addresses.contains(address);
    }
    
    /**
     * 获取负载均衡统计信息
     */
    public String getLoadBalanceStats() {
        return loadBalanceStrategy.getStats() != null ? 
                loadBalanceStrategy.getStats().toString() : "无统计信息";
    }
    
    /**
     * 重置负载均衡状态
     */
    public void resetLoadBalanceState() {
        loadBalanceStrategy.reset();
        log.info("重置CONFIG路由目标负载均衡状态: {}", serviceName);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigRouteTarget that = (ConfigRouteTarget) o;
        return Objects.equals(serviceId, that.serviceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }
    
    @Override
    public String toString() {
        return String.format(
            "ConfigRouteTarget{serviceId='%s', serviceName='%s', protocol=%s, addresses=%d, strategy='%s'}",
            serviceId, serviceName, supportProtocol.type(), addresses.size(), loadBalanceStrategy.getStrategyName()
        );
    }
} 