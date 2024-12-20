package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.filter.FilterTypeEnum;
import com.muxin.gateway.core.filter.RouteRuleFilter;
import com.muxin.gateway.core.filter.GatewayFilterChain;
import com.muxin.gateway.core.route.RouteLocator;
import com.muxin.gateway.core.route.RouteRule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 10:11
 */
public class ChainBasedExchangeHandler implements ExchangeHandler {

    private final RouteLocator routeLocator;

    public ChainBasedExchangeHandler(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }


    /**
     * 1.查找路由
     * 2.拼装filter
     * 3.执行filter
     * 4.处理返回
     *
     * @param exchange
     */
    @Override
    public void handle(ServerWebExchange exchange) {
        //查找路由
        RouteRule routeRule = lookupRoute(exchange);
        //组装Filter
        List<RouteRuleFilter> ruleFilters = routeRule.getRouteRuleFilters();
        Map<FilterTypeEnum, List<RouteRuleFilter>> filterTypeEnumListMap = ruleFilters.stream()
                .collect(Collectors.groupingBy(
                        // 按 filterType 分组
                        RouteRuleFilter::filterType,
                        // 每组的值为按 order 排序后的 List
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparingInt(Ordered::getOrder))
                                        .collect(Collectors.toList())
                        )
                ));
        //打印filter
        printFilter(filterTypeEnumListMap);
        //request phase
        if (Objects.nonNull(filterTypeEnumListMap.get(FilterTypeEnum.REQUEST))) {
            for (RouteRuleFilter filter : filterTypeEnumListMap.get(FilterTypeEnum.REQUEST)) {
                filter.filter(exchange);
            }
        }
        //endpoint phase
        if (Objects.nonNull(filterTypeEnumListMap.get(FilterTypeEnum.ENDPOINT))) {
            for (RouteRuleFilter filter : filterTypeEnumListMap.get(FilterTypeEnum.ENDPOINT)) {
                filter.filter(exchange);
            }
        }
        //response phase
        if (Objects.nonNull(filterTypeEnumListMap.get(FilterTypeEnum.RESPONSE))) {
            for (RouteRuleFilter filter : filterTypeEnumListMap.get(FilterTypeEnum.RESPONSE)) {
                filter.filter(exchange);
            }
        }
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
                .orElse(route404());
    }


    /**
     * 打印filter
     *
     * @param filterTypeEnumListMap
     */
    protected void printFilter(Map<FilterTypeEnum, List<RouteRuleFilter>> filterTypeEnumListMap) {


    }


    /**
     * 默认404路由
     *
     * @return
     */
    protected RouteRule route404() {

        return null;
    }


    public static class DefaultGatewayFilterChain implements GatewayFilterChain {

        private final List<RouteRuleFilter> filters;

        public DefaultGatewayFilterChain(List<RouteRuleFilter> filters) {
            this.filters = filters;
        }

        @Override
        public void filter(ServerWebExchange exchange) {
            for (RouteRuleFilter filter : filters) {
                filter.filter(exchange);
            }
        }

    }


}
