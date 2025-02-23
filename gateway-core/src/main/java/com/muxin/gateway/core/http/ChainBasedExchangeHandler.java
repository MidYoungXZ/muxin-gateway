package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.filter.FilterTypeEnum;
import com.muxin.gateway.core.filter.GatewayFilterChain;
import com.muxin.gateway.core.filter.RouteRuleFilter;
import com.muxin.gateway.core.route.RouteLocator;
import com.muxin.gateway.core.route.RouteRule;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理HTTP请求的链式处理器，通过查找路由、组装过滤器、执行过滤器和处理返回来完成请求的处理。
 *
 * @author Administrator
 * @date 2024/11/21 10:11
 */
@Slf4j
public class ChainBasedExchangeHandler implements ExchangeHandler {

    private final RouteLocator routeLocator;

    public ChainBasedExchangeHandler(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    /**
     * 处理HTTP请求的主方法，包括查找路由、组装过滤器、执行过滤器和处理返回。
     *
     * @param exchange 当前的ServerWebExchange对象，包含了请求和响应的相关信息。
     */
    @Override
    public void handle(ServerWebExchange exchange) {
        try {
            // 根据请求查找对应的路由规则
            RouteRule routeRule = lookupRoute(exchange);
            // 获取路由规则中的过滤器列表
            List<RouteRuleFilter> ruleFilters = routeRule.getRouteRuleFilters();
            // 创建默认的过滤器链并执行过滤器
            new DefaultGatewayFilterChain(ruleFilters).filter(exchange);
        } catch (Throwable throwable) {
            // 捕获并记录处理过程中发生的异常
            log.error("Request handle failed.", throwable);
//            HttpServerResponse response = ResponseUtil.createEmptyResponse(ResponseStatusEnum.G00_05_0005);
//            exchange.setResponse(response);
        } finally {
            // 释放请求资源
            ReferenceCountUtil.release(exchange.getRequest());
            // 将响应写回客户端并关闭channel
            exchange.inboundContext()
                    .writeAndFlush(exchange.getResponse())
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 根据请求查找对应的路由规则。
     *
     * @param exchange 当前的ServerWebExchange对象，包含了请求和响应的相关信息。
     * @return 查找到的路由规则，如果没有找到则返回404路由规则。
     */
    protected RouteRule lookupRoute(ServerWebExchange exchange) {
        return Optional.of(routeLocator.getRoutes(exchange.getRequest().uri()))
                .orElse(Collections.emptyList())
                .stream()
                .filter(r -> r.getPredicate().test(exchange))
                .findFirst()
                .orElse(RouteRule.ROUTE_404);
    }

    /**
     * 默认的过滤器链实现，根据过滤器类型和顺序执行过滤器。
     */
    public static class DefaultGatewayFilterChain implements GatewayFilterChain {

        private final Map<FilterTypeEnum, List<RouteRuleFilter>> filterTypeEnumListMap;

        /**
         * 构造函数，根据过滤器列表初始化过滤器链。
         *
         * @param ruleFilters 过滤器列表。
         */
        public DefaultGatewayFilterChain(List<RouteRuleFilter> ruleFilters) {
            this.filterTypeEnumListMap = ruleFilters.stream()
                    .collect(Collectors.groupingBy(
                            RouteRuleFilter::filterType,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .sorted(Comparator.comparingInt(Ordered::getOrder))
                                            .collect(Collectors.toList())
                            )
                    ));
        }

        /**
         * 执行过滤器链中的过滤器。
         *
         * @param exchange 当前的ServerWebExchange对象，包含了请求和响应的相关信息。
         */
        @Override
        public void filter(ServerWebExchange exchange) {
            for (FilterTypeEnum phase : FilterTypeEnum.values()) {
                List<RouteRuleFilter> filters = filterTypeEnumListMap.get(phase);
                if (Objects.nonNull(filters)) {
                    for (RouteRuleFilter filter : filters) {
                        filter.filter(exchange);
                    }
                }
            }
        }
    }
}
