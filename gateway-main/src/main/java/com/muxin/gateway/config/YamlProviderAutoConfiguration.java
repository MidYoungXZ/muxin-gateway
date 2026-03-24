package com.muxin.gateway.config;

import com.muxin.gateway.core.config.provider.RouteConfigProvider;
import com.muxin.gateway.core.config.provider.ServiceConfigProvider;
import com.muxin.gateway.core.config.provider.YamlRouteConfigProvider;
import com.muxin.gateway.core.config.provider.YamlServiceConfigProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class YamlProviderAutoConfiguration {

    @Value("${muxin.gateway.config-file:gateway-routes.yml}")
    private String configFile;

    @Value("${muxin.gateway.config-watch-enabled:true}")
    private boolean watchEnabled;

    @Bean
    @ConditionalOnMissingBean(RouteConfigProvider.class)
    public RouteConfigProvider yamlRouteConfigProvider() {
        log.info("[YamlProviderAutoConfiguration] Creating YamlRouteConfigProvider");
        YamlRouteConfigProvider provider = new YamlRouteConfigProvider(configFile);
        if (watchEnabled) {
            provider.startWatching();
        }
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean(ServiceConfigProvider.class)
    public ServiceConfigProvider yamlServiceConfigProvider() {
        log.info("[YamlProviderAutoConfiguration] Creating YamlServiceConfigProvider");
        YamlServiceConfigProvider provider = new YamlServiceConfigProvider(configFile);
        if (watchEnabled) {
            provider.startWatching();
        }
        return provider;
    }
}