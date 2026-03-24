package com.muxin.gateway.config;

import com.muxin.gateway.core.GatewayBootstrap;
import com.muxin.gateway.core.config.provider.RouteConfigProvider;
import com.muxin.gateway.core.config.provider.ServiceConfigProvider;
import com.muxin.gateway.core.server.HttpServerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties({GatewayProperties.class})
@ConditionalOnProperty(prefix = "muxin.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class GatewayAutoConfiguration {

    @Autowired(required = false)
    private RouteConfigProvider routeConfigProvider;

    @Autowired(required = false)
    private ServiceConfigProvider serviceConfigProvider;

    @Bean
    public GatewayBootstrap gatewayBootstrap(GatewayProperties gatewayProperties) {
        log.info("[GatewayAutoConfiguration] Creating GatewayBootstrap bean");

        NettyServerProperties serverProps = gatewayProperties.getNetty();
        HttpServerConfig httpConfig = buildHttpServerConfig(serverProps);

        GatewayBootstrapWrapper bootstrap = new GatewayBootstrapWrapper();
        bootstrap.setServerPort(serverProps.getPort());
        bootstrap.setHttpServerConfig(httpConfig);

        if (routeConfigProvider != null) {
            bootstrap.setRouteConfigProvider(routeConfigProvider);
            log.info("[GatewayAutoConfiguration] RouteConfigProvider: {}", routeConfigProvider.getSource());
        }

        if (serviceConfigProvider != null) {
            bootstrap.setServiceConfigProvider(serviceConfigProvider);
            log.info("[GatewayAutoConfiguration] ServiceConfigProvider: {}", serviceConfigProvider.getSource());
        }

        return bootstrap;
    }

    private HttpServerConfig buildHttpServerConfig(NettyServerProperties props) {
        return HttpServerConfig.builder()
                .bossThreads(props.getBossThreads())
                .workerThreads(props.getWorkerThreads())
                .bossThreadName(props.getBossThreadName())
                .workerThreadName(props.getWorkerThreadName())
                .backlog(props.getBacklog())
                .reuseAddr(props.isReuseAddress())
                .keepAlive(props.isKeepAlive())
                .tcpNoDelay(props.isTcpNoDelay())
                .sendBufferSize(props.getSndBuf())
                .receiveBufferSize(props.getRcvBuf())
                .maxContentLength((int) Math.min(props.getMaxContentLength(), Integer.MAX_VALUE))
                .requestTimeout(props.getRequestTimeout())
                .connectionTimeout(props.getConnectionTimeout())
                .idleTimeout(props.getIdleTimeout())
                .compressionEnabled(props.isCompressionEnabled())
                .enableGracefulShutdown(true)
                .gracefulShutdownTimeout(30000L)
                .build();
    }
}