package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.common.ServiceRegistry;
import com.muxin.gateway.core.plus.message.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * DISCOVERY类型路由服务工厂
 * 负责根据服务定义创建和验证基于服务发现的路由服务
 * 负载均衡策略由Route级别管理
 *
 * @author muxin
 */
@Slf4j
public class DiscoveryRouteServiceFactory implements RouteServiceFactory {
    
    private final ServiceRegistry serviceRegistry;
    
    public DiscoveryRouteServiceFactory(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry不能为空");
    }
    
    @Override
    public ServiceType getSupportedType() {
        return ServiceType.DISCOVERY;
    }
    
    @Override
    public RouteService createRouteTarget(ServiceDefinition serviceDefinition) {
        log.debug("开始创建DISCOVERY路由服务: {}", serviceDefinition.getId());
        
        // 验证定义
        validateConfig(serviceDefinition);
        
        try {
            // 获取协议
            Protocol protocol = serviceDefinition.getSupportProtocol().toProtocol();
            
            // 创建DISCOVERY路由服务
            DiscoveryRouteService routeService = new DiscoveryRouteService(
                    serviceDefinition,
                    protocol,
                    serviceRegistry,
                    serviceDefinition.getConfig()
            );
            
            log.info("成功创建DISCOVERY路由服务: {} ({})", 
                    serviceDefinition.getName(), serviceDefinition.getId());
            
            return routeService;
            
        } catch (Exception e) {
            log.error("创建DISCOVERY路由服务失败: {}", serviceDefinition.getId(), e);
            throw new IllegalArgumentException("创建DISCOVERY路由服务失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void validateConfig(ServiceDefinition serviceDefinition) {
        log.debug("开始验证DISCOVERY路由服务定义: {}", serviceDefinition.getId());
        
        // 基础字段验证
        if (serviceDefinition.getType() != ServiceType.DISCOVERY) {
            throw new IllegalArgumentException("服务类型必须是DISCOVERY");
        }
        
        if (serviceDefinition.getId() == null || serviceDefinition.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定serviceId");
        }
        
        if (serviceDefinition.getName() == null || serviceDefinition.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定serviceName");
        }
        
        // 协议验证
        if (serviceDefinition.getSupportProtocol() == null) {
            throw new IllegalArgumentException("DISCOVERY类型必须指定协议");
        }
        
        // 服务发现配置验证
        validateDiscoveryConfig(serviceDefinition);
        
        log.debug("DISCOVERY路由服务定义验证通过: {}", serviceDefinition.getId());
    }
    
    /**
     * 验证服务发现配置
     */
    private void validateDiscoveryConfig(ServiceDefinition serviceDefinition) {
        Map<String, Object> config = serviceDefinition.getConfig();
        if (config == null) {
            return;
        }
        
        // 验证缓存过期时间
        Object cacheExpireTime = config.get("cache-expire-time");
        if (cacheExpireTime != null) {
            try {
                int seconds = Integer.parseInt(cacheExpireTime.toString());
                if (seconds < 5) {
                    throw new IllegalArgumentException("缓存过期时间不能少于5秒");
                }
                if (seconds > 3600) {
                    throw new IllegalArgumentException("缓存过期时间不能超过3600秒");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("缓存过期时间必须是数字: " + cacheExpireTime);
            }
        }
        
        // 验证健康检查配置
        validateHealthCheckConfig(config);
        
        // 验证服务发现特定配置
        validateServiceDiscoveryConfig(config);
    }
    
    /**
     * 验证健康检查配置
     */
    private void validateHealthCheckConfig(Map<String, Object> config) {
        Object healthCheck = config.get("health-check");
        if (healthCheck instanceof Map<?, ?>) {
            Map<?, ?> healthConfig = (Map<?, ?>) healthCheck;
            // 验证健康检查间隔
            Object interval = healthConfig.get("interval");
            if (interval != null) {
                try {
                    int seconds = Integer.parseInt(interval.toString());
                    if (seconds < 5 || seconds > 300) {
                        throw new IllegalArgumentException("健康检查间隔必须在5-300秒之间");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("健康检查间隔必须是数字: " + interval);
                }
            }
            
            // 验证超时时间
            Object timeout = healthConfig.get("timeout");
            if (timeout != null) {
                try {
                    int seconds = Integer.parseInt(timeout.toString());
                    if (seconds < 1 || seconds > 30) {
                        throw new IllegalArgumentException("健康检查超时时间必须在1-30秒之间");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("健康检查超时时间必须是数字: " + timeout);
                }
            }
        }
    }
    
    /**
     * 验证服务发现特定配置
     */
    private void validateServiceDiscoveryConfig(Map<String, Object> config) {
        // 验证服务发现注册中心配置
        Object registry = config.get("registry");
        if (registry instanceof Map<?, ?>) {
            Map<?, ?> registryConfig = (Map<?, ?>) registry;
            
            // 验证注册中心类型
            Object type = registryConfig.get("type");
            if (type != null && !isValidRegistryType(type.toString())) {
                throw new IllegalArgumentException("不支持的注册中心类型: " + type);
            }
            
            // 验证注册中心地址
            Object address = registryConfig.get("address");
            if (address != null && address.toString().trim().isEmpty()) {
                throw new IllegalArgumentException("注册中心地址不能为空");
            }
        }
        
        // 验证命名空间
        Object namespace = config.get("namespace");
        if (namespace != null && namespace.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("命名空间不能为空字符串");
        }
    }
    
    /**
     * 检查是否为有效的注册中心类型
     */
    private boolean isValidRegistryType(String type) {
        return "nacos".equalsIgnoreCase(type) ||
               "eureka".equalsIgnoreCase(type) ||
               "consul".equalsIgnoreCase(type) ||
               "zookeeper".equalsIgnoreCase(type);
    }
    
    @Override
    public String toString() {
        return "DiscoveryRouteServiceFactory{supportedType=" + getSupportedType() + 
               ", serviceRegistry=" + serviceRegistry.getClass().getSimpleName() + "}";
    }
} 