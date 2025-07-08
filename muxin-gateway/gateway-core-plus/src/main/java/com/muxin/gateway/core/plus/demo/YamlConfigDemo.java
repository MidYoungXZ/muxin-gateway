package com.muxin.gateway.core.plus.demo;

import com.muxin.gateway.core.plus.config.GatewayConfigLoader;
import com.muxin.gateway.core.plus.config.GatewayRouteConfig;
import com.muxin.gateway.core.plus.predicate.PredicateFactory;
import com.muxin.gateway.core.plus.route.EnhancedRoute;
import com.muxin.gateway.core.plus.route.EnhancedRouteConfig;
import com.muxin.gateway.core.plus.route.RouteConfigConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * YAML配置演示类
 * 展示如何加载和使用新的配置系统
 *
 * @author muxin
 */
@Slf4j
public class YamlConfigDemo {
    
    public static void main(String[] args) {
        YamlConfigDemo demo = new YamlConfigDemo();
        demo.run();
    }
    
    public void run() {
        try {
            log.info("🚀 开始YAML配置演示...");
            
            // 1. 加载配置
            loadConfiguration();
            
            // 2. 演示配置转换
            demonstrateConfigConversion();
            
            // 3. 演示路由匹配
            demonstrateRouteMatching();
            
            log.info("✅ YAML配置演示完成");
            
        } catch (Exception e) {
            log.error("❌ YAML配置演示失败", e);
        }
    }
    
    /**
     * 演示配置加载
     */
    private void loadConfiguration() {
        log.info("📝 演示配置加载...");
        
        try {
            // 创建配置加载器
            GatewayConfigLoader loader = new GatewayConfigLoader();
            
            // 加载配置文件
            GatewayRouteConfig config = loader.loadConfig();
            
            // 显示配置信息
            log.info("配置加载成功:");
            log.info("- 路由数量: {}", config.getRoutes() != null ? config.getRoutes().size() : 0);
            
            if (config.getGateway() != null && config.getGateway().getServer() != null) {
                log.info("- HTTP端口: {}", config.getGateway().getServer().getHttp().getPort());
            }
            
            if (config.getRoutes() != null) {
                for (EnhancedRouteConfig routeConfig : config.getRoutes()) {
                    log.info("- 路由: {} ({}) - {}", 
                            routeConfig.getId(), 
                            routeConfig.getName(),
                            routeConfig.getTarget().getType());
                }
            }
            
        } catch (Exception e) {
            log.warn("配置文件加载失败，可能是文件不存在: {}", e.getMessage());
            
            // 演示从字符串加载配置
            demonstrateStringConfig();
        }
    }
    
    /**
     * 演示从字符串加载配置
     */
    private void demonstrateStringConfig() {
        log.info("📝 演示从字符串加载配置...");
        
        String yamlConfig = """
                gateway:
                  server:
                    http:
                      port: 8080
                      
                routes:
                  - id: test-route
                    name: "测试路由"
                    description: "简单的测试路由"
                    order: 100
                    enabled: true
                    
                    inbound-protocol:
                      type: HTTP
                      version: "1.1"
                      
                    predicates:
                      - type: PATH
                        config:
                          pattern: "/api/test/**"
                      - type: METHOD
                        config:
                          methods: ["GET", "POST"]
                    
                    filters:
                      - type: REQUEST_LOG
                        order: 100
                        enabled: true
                    
                    target:
                      type: STATIC
                      outbound-protocol:
                        type: HTTP
                        version: "1.1"
                      addresses:
                        - uri: "http://test-service:8080"
                          weight: 100
                      load-balance:
                        strategy: "ROUND_ROBIN"
                    
                    timeouts:
                      connection: 5s
                      request: 30s
                      total: 60s
                      
                    metadata:
                      service-name: "test-service"
                """;
        
        try {
            GatewayConfigLoader loader = new GatewayConfigLoader();
            GatewayRouteConfig config = loader.loadConfigFromString(yamlConfig);
            
            log.info("从字符串加载配置成功:");
            log.info("- 路由数量: {}", config.getRoutes().size());
            
            EnhancedRouteConfig route = config.getRoutes().get(0);
            log.info("- 路由ID: {}", route.getId());
            log.info("- 路由名称: {}", route.getName());
            log.info("- 入站协议: {}", route.getInboundProtocol().getType());
            log.info("- 出站协议: {}", route.getTarget().getOutboundProtocol().getType());
            log.info("- 目标类型: {}", route.getTarget().getType());
            log.info("- 断言数量: {}", route.getPredicates().size());
            log.info("- 过滤器数量: {}", route.getFilters().size());
            
        } catch (Exception e) {
            log.error("从字符串加载配置失败", e);
        }
    }
    
    /**
     * 演示配置转换
     */
    private void demonstrateConfigConversion() {
        log.info("🔄 演示配置转换...");
        
        // 由于PredicateFactory的实现可能还没完成，这里先跳过
        log.info("配置转换演示跳过（需要完整的PredicateFactory实现）");
        
        /*
        try {
            // 创建简单的PredicateFactory（演示用）
            PredicateFactory predicateFactory = new SimplePredicateFactory();
            
            // 创建配置转换器
            RouteConfigConverter converter = new RouteConfigConverter(predicateFactory);
            
            // 这里可以添加转换演示代码
            
        } catch (Exception e) {
            log.error("配置转换演示失败", e);
        }
        */
    }
    
    /**
     * 演示路由匹配
     */
    private void demonstrateRouteMatching() {
        log.info("🎯 演示路由匹配...");
        
        // 创建测试用的协议转换逻辑
        demonstrateProtocolConversion();
        
        // 演示静态vs服务发现的区别
        demonstrateTargetTypes();
        
        // 演示负载均衡配置
        demonstrateLoadBalanceConfig();
    }
    
    /**
     * 演示协议转换
     */
    private void demonstrateProtocolConversion() {
        log.info("📡 演示协议转换逻辑...");
        
        // HTTP -> HTTP (无转换)
        log.info("HTTP -> HTTP: 无需转换");
        
        // HTTP -> gRPC (需要转换)
        log.info("HTTP -> gRPC: 需要协议转换");
        
        // WebSocket -> TCP (需要转换)
        log.info("WebSocket -> TCP: 需要协议转换");
        
        // 不支持的转换
        log.info("TCP -> HTTP: 暂不支持的转换");
    }
    
    /**
     * 演示目标类型
     */
    private void demonstrateTargetTypes() {
        log.info("🎯 演示目标类型...");
        
        log.info("STATIC类型:");
        log.info("- 支持多个静态地址");
        log.info("- 支持权重配置");
        log.info("- 支持多种负载均衡策略");
        
        log.info("DISCOVERY类型:");
        log.info("- 只支持一个lb://service-name地址");
        log.info("- 权重来自注册中心");
        log.info("- 支持服务发现");
    }
    
    /**
     * 演示负载均衡配置
     */
    private void demonstrateLoadBalanceConfig() {
        log.info("⚖️ 演示负载均衡配置...");
        
        log.info("支持的策略:");
        log.info("- ROUND_ROBIN: 轮询");
        log.info("- WEIGHTED_ROUND_ROBIN: 加权轮询");
        log.info("- LEAST_CONNECTIONS: 最少连接");
        log.info("- CONSISTENT_HASH: 一致性哈希");
        
        log.info("权重来源:");
        log.info("- STATIC类型: 配置文件中的weight字段");
        log.info("- DISCOVERY类型: 注册中心的weight-metadata-key字段");
    }
} 