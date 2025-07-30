package com.muxin.gateway.spring.boot.starter;

import com.muxin.gateway.core.plus.GatewayBootstrap;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关自动配置类 - 极简版本
 * 仅负责启动和配置映射，不涉及Web服务器
 * 
 * @author muxin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "gateway.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GatewaySpringProperties.class)
public class GatewayAutoConfiguration {

    /**
     * 创建网关引导器Bean
     * 使用Spring的Bean生命周期管理，不手动调用init/start方法
     */
    @Bean
    @ConditionalOnMissingBean
    public GatewayBootstrap gatewayBootstrap(GatewaySpringProperties springProperties) {
        log.info("Creating GatewayBootstrap with Spring properties");
        
        // 将Spring配置转换为Core配置
        GatewayConfig coreConfig = convertToGatewayConfig(springProperties);
        
        // 创建网关引导器
        GatewayBootstrap bootstrap = new GatewayBootstrap();
        
        // 注意：这里不调用init/start方法，让生命周期管理器处理
        log.debug("GatewayBootstrap created successfully");
        
        return bootstrap;
    }

    /**
     * 创建生命周期管理器Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public GatewayLifecycleManager gatewayLifecycleManager(GatewayBootstrap gatewayBootstrap, 
                                                          GatewaySpringProperties springProperties) {
        log.info("Creating GatewayLifecycleManager");
        
        // 将配置传递给生命周期管理器
        GatewayConfig coreConfig = convertToGatewayConfig(springProperties);
        
        return new GatewayLifecycleManager(gatewayBootstrap, coreConfig);
    }

    /**
     * 配置转换器 - 将Spring配置转换为Core配置
     */
    private GatewayConfig convertToGatewayConfig(GatewaySpringProperties springProperties) {
        log.debug("Converting Spring properties to Gateway core config");
        
        return GatewayConfig.builder()
                .coreConfig(springProperties.getCore())
                .connectionPoolConfig(springProperties.getConnectionPool())
                .routeConfig(springProperties.getRoute())
                .filterConfig(springProperties.getFilter())
                .loadBalanceConfig(springProperties.getLoadBalance())
                .nodeManagerConfig(springProperties.getNodeManager())
                .protocolConverterConfig(springProperties.getProtocolConverter())
                .serverConfig(springProperties.getServer())
                .build();
    }
} 