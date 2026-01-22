package com.muxin.gateway.core.plus.config;

import com.muxin.gateway.core.plus.route.RouteDefinition;
import com.muxin.gateway.core.plus.route.ServiceDefinition;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网关配置加载器（v2.0）
 * 支持新的YAML配置结构
 * - 独立的服务定义（services）
 * - 路通过 service-ref 引用服务
 * - 功能域配置（domains）
 * - 枚举定义（enums）
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class GatewayConfigLoader {
    
    private static final String DEFAULT_CONFIG_FILE = "gateway-routes.yml";
    private final Yaml yaml;
    
    public GatewayConfigLoader() {
        this.yaml = new Yaml();
    }
    
    /**
     * 从默认配置文件加载配置
     */
    public GatewayRouteConfig loadConfig() {
        return loadConfig(DEFAULT_CONFIG_FILE);
    }
    
    /**
     * 从指定配置文件加载配置
     */
    public GatewayRouteConfig loadConfig(String configFile) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (inputStream == null) {
                log.warn("配置文件 {} 不存在，使用默认配置", configFile);
                return createDefaultConfig();
            }
            
            GatewayRouteConfig config = yaml.load(inputStream);
            
            // 处理服务引用
            resolveServiceReferences(config);
            
            validateConfig(config);
            
            log.info("成功加载配置文件: {}, 共 {} 个服务定义, {} 个路由配置", 
                    configFile,
                    config.getServices() != null ? config.getServices().size() : 0,
                    config.getRoutes() != null ? config.getRoutes().size() : 0);
            
            return config;
            
        } catch (Exception e) {
            log.error("加载配置文件失败: {}", configFile, e);
            throw new IllegalStateException("无法加载配置文件: " + configFile, e);
        }
    }
    
    /**
     * 从字符串加载配置（用于测试）
     */
    public GatewayRouteConfig loadConfigFromString(String yamlContent) {
        try {
            GatewayRouteConfig config = yaml.load(yamlContent);
            resolveServiceReferences(config);
            validateConfig(config);
            return config;
        } catch (Exception e) {
            log.error("解析YAML配置失败", e);
            throw new IllegalArgumentException("无效的YAML配置", e);
        }
    }
    
    /**
     * 解析服务引用
     * 将 routes 中的 service-ref 解析为实际的服务定义
     */
    private void resolveServiceReferences(GatewayRouteConfig config) {
        if (config.getRoutes() == null || config.getRoutes().isEmpty()) {
            return;
        }
        
        if (config.getServices() == null || config.getServices().isEmpty()) {
            log.warn("服务定义为空，但路由引用了服务");
            return;
        }
        
        // 构建服务ID到服务定义的映射
        Map<String, ServiceDefinition> serviceMap = config.getServices().stream()
                .collect(Collectors.toMap(
                        ServiceDefinition::getId,
                        service -> service,
                        (existing, replacement) -> {
                            log.warn("服务ID重复: {}，将使用第一个定义", existing.getId());
                            return existing;
                        }
                ));
        
        // 解析每个路由的服务引用
        for (RouteDefinition route : config.getRoutes()) {
            String serviceRef = route.getServiceRef();
            if (serviceRef != null && !serviceRef.trim().isEmpty()) {
                ServiceDefinition service = serviceMap.get(serviceRef);
                if (service == null) {
                    throw new IllegalArgumentException(
                            String.format("路由 %s 引用的服务不存在: %s", route.getId(), serviceRef));
                }
                // 在内存中保存服务定义的引用（可选）
                log.debug("路由 {} 引用服务: {}", route.getId(), serviceRef);
            }
        }
        
        log.info("服务引用解析完成，共 {} 个服务定义，{} 个路由引用", 
                serviceMap.size(), 
                config.getRoutes().stream()
                        .filter(r -> r.getServiceRef() != null && !r.getServiceRef().trim().isEmpty())
                        .count());
    }
    
    /**
     * 验证配置
     */
    private void validateConfig(GatewayRouteConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置不能为空");
        }
        
        // 验证服务定义
        if (config.getServices() != null) {
            for (ServiceDefinition service : config.getServices()) {
                try {
                    service.validate();
                } catch (Exception e) {
                    log.error("服务配置验证失败: {}", service.getId(), e);
                    throw new IllegalArgumentException("服务配置验证失败: " + service.getId(), e);
                }
            }
        }
        
        // 验证路由配置
        if (config.getRoutes() != null) {
            for (RouteDefinition route : config.getRoutes()) {
                try {
                    route.validate();
                } catch (Exception e) {
                    log.error("路由配置验证失败: {}", route.getId(), e);
                    throw new IllegalArgumentException("路由配置验证失败: " + route.getId(), e);
                }
            }
        }
        
        log.debug("配置验证通过");
    }
    
    /**
     * 创建默认配置
     */
    private GatewayRouteConfig createDefaultConfig() {
        log.info("创建默认配置");
        
        return GatewayRouteConfig.builder()
                .domains(createDefaultDomainsConfig())
                .services(List.of())
                .routes(List.of())
                .build();
    }
    
    /**
     * 创建默认功能域配置
     */
    private GatewayRouteConfig.DomainsConfig createDefaultDomainsConfig() {
        return GatewayRouteConfig.DomainsConfig.builder()
                .core(GatewayRouteConfig.CoreConfig.builder()
                        .defaultTimeout("30s")
                        .maxRequestSize("10MB")
                        .maxResponseSize("50MB")
                        .build())
                .threadPools(GatewayRouteConfig.ThreadPoolConfig.builder()
                        .business(GatewayRouteConfig.BusinessThreadPoolConfig.builder()
                                .coreSize(16)
                                .maxSize(32)
                                .queueCapacity(1000)
                                .keepAlive("60s")
                                .build())
                        .build())
                .servers(GatewayRouteConfig.ServerConfig.builder()
                        .http(GatewayRouteConfig.HttpServerConfig.builder()
                                .port(8080)
                                .maxContentLength("10MB")
                                .keepAlive(true)
                                .compression(true)
                                .build())
                        .management(GatewayRouteConfig.ManagementConfig.builder()
                                .port(8081)
                                .enabled(true)
                                .build())
                        .build())
                .connectionPools(GatewayRouteConfig.ConnectionPoolConfig.builder()
                        .defaultConfig(GatewayRouteConfig.DefaultConnectionPoolConfig.builder()
                                .maxConnectionsPerHost(100)
                                .maxIdleConnections(50)
                                .connectionTimeout("5s")
                                .idleTimeout("60s")
                                .build())
                        .build())
                .build();
    }
    
    /**
     * 获取服务定义映射
     * 供其他模块使用
     */
    public Map<String, ServiceDefinition> getServiceMap(GatewayRouteConfig config) {
        if (config.getServices() == null) {
            return new HashMap<>();
        }
        
        return config.getServices().stream()
                .collect(Collectors.toMap(ServiceDefinition::getId, s -> s));
    }
    
    /**
     * 获取路由配置
     * 供其他模块使用
     */
    public List<RouteDefinition> getRoutes(GatewayRouteConfig config) {
        if (config.getRoutes() == null) {
            return List.of();
        }
        return config.getRoutes();
    }
    
    /**
     * 重新加载配置
     */
    public GatewayRouteConfig reloadConfig() {
        log.info("重新加载配置文件");
        return loadConfig();
    }
    
    /**
     * 保存配置到文件（暂不实现）
     */
    public void saveConfig(GatewayRouteConfig config, String configFile) {
        // TODO: 实现配置保存功能
        throw new UnsupportedOperationException("配置保存功能暂未实现");
    }
}