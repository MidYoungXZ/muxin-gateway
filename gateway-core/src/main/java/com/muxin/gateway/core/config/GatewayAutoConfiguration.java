package com.muxin.gateway.core.config;

import com.muxin.gateway.core.factory.FilterFactory;
import com.muxin.gateway.core.factory.PredicateFactory;
import com.muxin.gateway.core.factory.impl.*;
import com.muxin.gateway.core.filter.MockEndpointFilter;
import com.muxin.gateway.core.http.ChainBasedExchangeHandler;
import com.muxin.gateway.core.http.ExchangeHandler;
import com.muxin.gateway.core.loadbalance.DefaultLoadBalanceFactory;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalance;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalanceFactory;
import com.muxin.gateway.core.loadbalance.RoundRobinLoadBalancer;
import com.muxin.gateway.core.netty.NettyHttpServer;
import com.muxin.gateway.core.route.InMemoryRouteDefinitionRepository;
import com.muxin.gateway.core.route.RouteDefinitionRepository;
import com.muxin.gateway.core.route.RouteDefinitionRouteLocator;
import com.muxin.gateway.core.route.RouteLocator;
import com.muxin.gateway.registry.api.RegisterCenter;
import com.muxin.gateway.registry.nacos.NacosRegisterCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.muxin.gateway.core.common.GatewayConstants.*;

/**
 * 网关自动配置类
 */
@Configuration
@EnableConfigurationProperties({GatewayProperties.class, NettyHttpServerProperties.class, NettyHttpClientProperties.class})
@Slf4j
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RouteDefinitionRepository routeDefinitionRepository(GatewayProperties gatewayProperties) {
        return new InMemoryRouteDefinitionRepository(gatewayProperties.getRoutes());
    }

    @Bean
    @ConditionalOnMissingBean
    public Map<String, PredicateFactory> predicateFactoryMap() {
        Map<String, PredicateFactory> map = new HashMap<>();
        
        PathPredicateFactory pathFactory = new PathPredicateFactory();
        map.put(pathFactory.name(), pathFactory);
        
        MethodPredicateFactory methodFactory = new MethodPredicateFactory();
        map.put(methodFactory.name(), methodFactory);
        
        log.info(REGISTERED_PREDICATE_FACTORIES_LOG, map.size());
        return map;
    }

    @Bean
    @ConditionalOnMissingBean
    public Map<String, FilterFactory> filterFactoryMap() {
        Map<String, FilterFactory> map = new HashMap<>();
        
        map.put(STRIP_PREFIX_FILTER_NAME, new StripPrefixFilterFactory());
        
        log.info(REGISTERED_FILTER_FACTORIES_LOG, map.size());
        return map;
    }

    @Bean
    @ConditionalOnMissingBean
    public RouteLocator routeLocator(RouteDefinitionRepository repository,
                                   Map<String, FilterFactory> filterFactoryMap,
                                   Map<String, PredicateFactory> predicateFactoryMap) {
        RouteDefinitionRouteLocator locator = new RouteDefinitionRouteLocator(
            repository, filterFactoryMap, predicateFactoryMap);
        locator.init();
        return locator;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "muxin.gateway.register.type", havingValue = NACOS_REGISTER_TYPE, matchIfMissing = true)
    public RegisterCenter nacosRegisterCenter(@Value("${muxin.gateway.register.address:localhost:8848}") String address,
                                            @Value("${muxin.gateway.register.group:" + DEFAULT_GROUP + "}") String group) {
        return new NacosRegisterCenter(address, group, DEFAULT_NAMESPACE);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayLoadBalanceFactory loadBalanceFactory(RegisterCenter registerCenter) {
        Map<String, GatewayLoadBalance> balancers = new HashMap<>();
        balancers.put(ROUND_ROBIN_BALANCER, new RoundRobinLoadBalancer(registerCenter));
        
        DefaultLoadBalanceFactory factory = new DefaultLoadBalanceFactory();
        factory.setGatewayLoadBalanceMap(balancers);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ExchangeHandler exchangeHandler(RouteLocator routeLocator) {
        return new ChainBasedExchangeHandler(routeLocator);
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyHttpServer nettyHttpServer(ExchangeHandler exchangeHandler,
                                         NettyHttpServerProperties properties) {
        return new NettyHttpServer(exchangeHandler, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MockEndpointFilter mockEndpointFilter() {
        return new MockEndpointFilter();
    }
}
