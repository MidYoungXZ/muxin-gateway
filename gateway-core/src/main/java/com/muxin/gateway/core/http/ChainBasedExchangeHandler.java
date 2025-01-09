package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.common.ResponseStatusEnum;
import com.muxin.gateway.core.filter.FilterTypeEnum;
import com.muxin.gateway.core.filter.GatewayFilterChain;
import com.muxin.gateway.core.filter.RouteRuleFilter;
import com.muxin.gateway.core.route.RouteLocator;
import com.muxin.gateway.core.route.RouteRule;
import com.muxin.gateway.core.utils.ResponseUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [Class description]
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
     * 处理请求
     *
     * @param exchange
     */
    @Override
    public void handle(ServerWebExchange exchange) {
        try {
            doHandle(exchange);
        } catch (Throwable throwable) {
            log.error("Request handle failed.", throwable);
            HttpServerResponse response = ResponseUtil.createEmptyResponse(ResponseStatusEnum.G00_05_0005);
            exchange.setResponse(response);
        }finally {
            //释放资源
            ReferenceCountUtil.release(exchange.getRequest());
            //写回
            exchange.inboundContext()
                    .writeAndFlush(exchange.getResponse())
                    .addListener(ChannelFutureListener.CLOSE); //释放资源后关闭channel
        }
    }

    /**
     * 1.查找路由
     * 2.拼装filter
     * 3.执行filter
     * 4.处理返回
     *
     * @param exchange
     */
    private void doHandle(ServerWebExchange exchange) {
        //查找路由
        RouteRule routeRule = lookupRoute(exchange);
        //组装Filter
        List<RouteRuleFilter> ruleFilters = routeRule.getRouteRuleFilters();
        new DefaultGatewayFilterChain(ruleFilters).filter(exchange);
    }

    /**
     * 查找路由
     *
     * @param exchange
     * @return
     */
    protected RouteRule lookupRoute(ServerWebExchange exchange) {
        return Optional.of(routeLocator.getRoutes(exchange.getRequest().uri()))
                .orElse(Collections.emptyList())
                .stream()
                .filter(r -> r.getPredicate().test(exchange))
                .findFirst()
                .orElse(RouteRule.ROUTE_404);
    }

    public static class DefaultGatewayFilterChain implements GatewayFilterChain {

        private final Map<FilterTypeEnum, List<RouteRuleFilter>> filterTypeEnumListMap;

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
