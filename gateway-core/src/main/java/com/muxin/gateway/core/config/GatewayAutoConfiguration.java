package com.muxin.gateway.core.config;

import com.muxin.gateway.core.filter.MockEndpointFilter;
import com.muxin.gateway.core.route.InMemoryRouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 15:13
 */
@Configuration
public class GatewayAutoConfiguration {

    @Bean
    public InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository(GatewayProperties gatewayProperties) {
        return new InMemoryRouteDefinitionRepository(gatewayProperties.getRoutes());
    }


    @Bean
    public MockEndpointFilter mockEndpointFilter() {
        return new MockEndpointFilter();
    }

}
