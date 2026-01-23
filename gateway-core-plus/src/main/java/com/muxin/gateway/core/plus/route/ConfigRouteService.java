package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CONFIG类型路由目标实现
 * 使用静态配置的地址列表，负载均衡由外部Route管理
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class ConfigRouteService implements RouteService {
    
    // ========== 服务定义 ==========
    private final ServiceDefinition serviceDefinition;
    
    // ========== 协议和地址 ==========
    private final Protocol supportProtocol;
    private final List<EndpointAddress> addresses;
    private final Map<String, Object> config;
    
    public ConfigRouteService(ServiceDefinition serviceDefinition,
                              Protocol supportProtocol,
                              List<EndpointAddress> addresses,
                              Map<String, Object> config) {
        this.serviceDefinition = Objects.requireNonNull(serviceDefinition, "serviceDefinition不能为空");
        this.supportProtocol = Objects.requireNonNull(supportProtocol, "supportProtocol不能为空");
        this.addresses = Objects.requireNonNull(addresses, "addresses不能为空");
        this.config = config;
        
        // 验证服务类型
        if (!serviceDefinition.isConfigType()) {
            throw new IllegalArgumentException("ConfigRouteService只支持CONFIG类型服务");
        }
        
        // 验证地址列表
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("CONFIG类型服务必须配置至少一个地址");
        }
        
        log.info("创建CONFIG路由服务: {} - {} addresses", serviceDefinition.getName(), addresses.size());
    }
    
    // ========== RouteService 接口实现 ==========
    
    @Override
    public ServiceDefinition serviceDefinition() {
        return serviceDefinition;
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
    public Map<String, Object> getTargetConfig() {
        return config;
    }
    
    @Override
    public EndpointAddress selectTarget(RequestContext context, LoadBalanceStrategy strategy) {
        // 1. 空地址检查
        if (addresses == null || addresses.isEmpty()) {
            log.error("[ConfigRouteService] 服务 {} 没有配置地址", serviceDefinition.getName());
            throw new IllegalStateException("服务 " + serviceDefinition.getName() + " 没有配置可用地址");
        }

        // 2. 策略空值检查 - 降级到第一个地址
        if (strategy == null) {
            log.warn("[ConfigRouteService] 负载均衡策略为空，使用第一个地址: {}", addresses.get(0).toUri());
            return addresses.get(0);
        }

        try {
            // 3. 正常选择
            EndpointAddress selected = strategy.select(addresses, context);
            log.debug("[ConfigRouteService] 选择目标: {} (策略: {}, 服务: {})",
                    selected.toUri(), strategy.getStrategyName(), serviceDefinition.getName());
            return selected;

        } catch (Exception e) {
            // 4. 降级处理：使用第一个地址
            log.warn("[ConfigRouteService] 负载均衡选择失败，使用第一个地址作为降级: {}, 错误: {}",
                    addresses.get(0).toUri(), e.getMessage());
            return addresses.get(0);
        }
    }
    
    // ========== 查询方法 ==========
    
    public String getServiceId() {
        return serviceDefinition.getId();
    }
    
    public String getServiceName() {
        return serviceDefinition.getName();
    }
    
    public ServiceType getServiceType() {
        return serviceDefinition.getType();
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigRouteService that = (ConfigRouteService) o;
        return Objects.equals(serviceDefinition.getId(), that.serviceDefinition.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(serviceDefinition.getId());
    }
    
    @Override
    public String toString() {
        return String.format(
            "ConfigRouteService{serviceId='%s', serviceName='%s', protocol=%s, addresses=%d}",
            serviceDefinition.getId(), serviceDefinition.getName(), supportProtocol.type(), addresses.size()
        );
    }
} 