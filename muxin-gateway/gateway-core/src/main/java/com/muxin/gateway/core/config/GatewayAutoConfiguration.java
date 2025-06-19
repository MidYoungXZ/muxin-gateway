package com.muxin.gateway.core.config;

import com.muxin.gateway.core.cache.RouteCache;
import com.muxin.gateway.core.http.ChainBasedExchangeHandler;
import com.muxin.gateway.core.http.ExchangeHandler;
import com.muxin.gateway.core.loadbalance.DefaultLoadBalanceFactory;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalance;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalanceFactory;
import com.muxin.gateway.core.loadbalance.RoundRobinLoadBalancer;
import com.muxin.gateway.core.netty.NettyHttpClient;
import com.muxin.gateway.core.netty.NettyHttpServer;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.route.InMemoryRouteDefinitionRepository;
import com.muxin.gateway.core.route.RouteDefinitionRepository;
import com.muxin.gateway.core.route.RouteDefinitionRouteLocator;
import com.muxin.gateway.core.route.RouteLocator;
import com.muxin.gateway.core.route.filter.GlobalFilter;
import com.muxin.gateway.core.route.filter.HttpProxyFilter;
import com.muxin.gateway.core.route.filter.LoadBalanceFilter;
import com.muxin.gateway.core.route.filter.factory.FilterFactory;
import com.muxin.gateway.core.route.filter.factory.StripPrefixFilterFactory;
import com.muxin.gateway.core.route.predicate.factory.MethodPredicateFactory;
import com.muxin.gateway.core.route.predicate.factory.PathPredicateFactory;
import com.muxin.gateway.core.route.predicate.factory.PredicateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.muxin.gateway.core.common.GatewayConstants.ROUND_ROBIN_BALANCER;
import static com.muxin.gateway.core.common.GatewayConstants.STRIP_PREFIX_FILTER_NAME;

/**
 * 网关自动配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@EnableScheduling
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RouteDefinitionRepository routeDefinitionRepository(GatewayProperties gatewayProperties) {
        return new InMemoryRouteDefinitionRepository(gatewayProperties.getRoutes());
    }

    @Bean
    @ConditionalOnMissingBean(name = "predicateFactoryMap")
    public Map<String, PredicateFactory> predicateFactoryMap() {
        Map<String, PredicateFactory> map = new HashMap<>();
        PathPredicateFactory pathFactory = new PathPredicateFactory();
        map.put(pathFactory.name(), pathFactory);
        MethodPredicateFactory methodFactory = new MethodPredicateFactory();
        map.put(methodFactory.name(), methodFactory);
        log.info("Registered {} predicate factories: {}", map.size(), map.keySet());
        return map;
    }

    @Bean
    @ConditionalOnMissingBean(name = "filterFactoryMap")
    public Map<String, FilterFactory> filterFactoryMap() {
        Map<String, FilterFactory> map = new HashMap<>();
        StripPrefixFilterFactory stripPrefixFactory = new StripPrefixFilterFactory();
        map.put(STRIP_PREFIX_FILTER_NAME, stripPrefixFactory);
        log.info("Registered {} filter factories: {}", map.size(), map.keySet());
        return map;
    }

    @Bean
    @ConditionalOnMissingBean
    public RouteLocator routeLocator(RouteDefinitionRepository repository,
                                     Map<String, FilterFactory> filterFactoryMap,
                                     Map<String, PredicateFactory> predicateFactoryMap,
                                     RouteCache routeCache) {
        RouteDefinitionRouteLocator locator = new RouteDefinitionRouteLocator(
                repository, filterFactoryMap, predicateFactoryMap, routeCache);
        locator.init();
        log.info("Creating RouteLocator with {} filter factories and {} predicate factories", filterFactoryMap.size(), predicateFactoryMap.size());
        return locator;
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
    public NettyHttpClient nettyHttpClient(GatewayProperties properties) {
        return new NettyHttpClient(properties.getNetty().getClient());
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalanceFilter loadBalanceFilter(GatewayLoadBalanceFactory loadBalanceFactory) {
        LoadBalanceFilter filter = new LoadBalanceFilter();
        filter.setGatewayLoadBalanceFactory(loadBalanceFactory);
        filter.setLoadBalanceType(ROUND_ROBIN_BALANCER);
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpProxyFilter httpProxyFilter(NettyHttpClient nettyHttpClient, GatewayProperties properties) {
        return new HttpProxyFilter(nettyHttpClient, properties.getNetty().getClient());
    }


    @Bean
    @ConditionalOnMissingBean
    public ExchangeHandler exchangeHandler(RouteLocator routeLocator,
                                           List<GlobalFilter> globalFilters) {
        log.info("Creating ExchangeHandler with {} global filters", globalFilters != null ? globalFilters.size() : 0);
        if (globalFilters != null) {
            log.debug("Registered global filters: {}", globalFilters.stream().map(f -> f.getClass().getSimpleName()).toArray());
        }
        return new ChainBasedExchangeHandler(routeLocator, globalFilters);
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyHttpServer nettyHttpServer(ExchangeHandler exchangeHandler,
                                           GatewayProperties properties) {
        return new NettyHttpServer(exchangeHandler, properties.getNetty().getServer());
    }
}
