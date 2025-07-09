package com.muxin.gateway.core.plus.config;

import com.muxin.gateway.core.plus.route.RouteDefinition;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;

/**
 * 网关配置加载器
 * 负责加载和解析YAML配置文件
 *
 * @author muxin
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
            validateConfig(config);
            
            log.info("成功加载配置文件: {}, 共 {} 个路由配置", configFile, 
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
            validateConfig(config);
            return config;
        } catch (Exception e) {
            log.error("解析YAML配置失败", e);
            throw new IllegalArgumentException("无效的YAML配置", e);
        }
    }
    
    /**
     * 验证配置
     */
    private void validateConfig(GatewayRouteConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置不能为空");
        }
        
        // 验证路由配置
        if (config.getRoutes() != null) {
            for (RouteDefinition routeConfig : config.getRoutes()) {
                try {
                    routeConfig.validate();
                } catch (Exception e) {
                    log.error("路由配置验证失败: {}", routeConfig.getId(), e);
                    throw new IllegalArgumentException("路由配置验证失败: " + routeConfig.getId(), e);
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
                .gateway(createDefaultGatewayConfig())
                .routes(List.of())
                .build();
    }
    
    /**
     * 创建默认网关配置
     */
    private GatewayRouteConfig.GatewayConfig createDefaultGatewayConfig() {
        return GatewayRouteConfig.GatewayConfig.builder()
                .core(GatewayRouteConfig.CoreConfig.builder()
                        .defaultTimeout("30s")
                        .maxRequestSize("10MB")
                        .maxResponseSize("50MB")
                        .businessThreadPool(GatewayRouteConfig.ThreadPoolConfig.builder()
                                .coreSize(16)
                                .maxSize(32)
                                .queueCapacity(1000)
                                .keepAlive("60s")
                                .build())
                        .connectionPool(GatewayRouteConfig.ConnectionPoolConfig.builder()
                                .maxConnectionsPerHost(100)
                                .maxIdleConnections(50)
                                .connectionTimeout("5s")
                                .idleTimeout("60s")
                                .build())
                        .build())
                .server(GatewayRouteConfig.ServerConfig.builder()
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
                .build();
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