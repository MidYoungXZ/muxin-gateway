package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.Ordered;
import com.muxin.gateway.core.route.RouteLocator;
import com.muxin.gateway.core.route.RouteRule;
import com.muxin.gateway.core.route.filter.*;
import com.muxin.gateway.core.utils.ExchangeUtil;
import com.muxin.gateway.core.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.muxin.gateway.core.common.GatewayConstants.SERVICE_ID;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 10:11
 */
@Slf4j
public class ChainBasedExchangeHandler implements ExchangeHandler {

    private final RouteLocator routeLocator;

    private final List<GlobalFilter> globalFilters;

    public ChainBasedExchangeHandler(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
        this.globalFilters = Collections.emptyList();
    }

    public ChainBasedExchangeHandler(RouteLocator routeLocator, List<GlobalFilter> globalFilters) {
        this.routeLocator = routeLocator;
        this.globalFilters = globalFilters != null ? globalFilters : Collections.emptyList();
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
            exchange.setOriginalResponse(ResponseUtil.error(throwable.getMessage()));
        }
    }

    /**
     * 1.查找路由
     * 2.拼装filter (合并GlobalFilter和RouteRuleFilter)
     * 3.执行filter
     * 4.处理返回
     *
     * @param exchange
     */
    private void doHandle(ServerWebExchange exchange) {
        //查找路由
        RouteRule routeRule = lookupRoute(exchange);

        // 从路由URI中提取服务ID（如果是lb://协议）
        if (routeRule.getUri() != null && ExchangeUtil.isLoadBalanceUri(routeRule.getUri())) {
            String serviceId = ExchangeUtil.extractServiceId(routeRule.getUri());
            if (serviceId != null) {
                exchange.setAttribute(SERVICE_ID, serviceId);
                log.debug("Set service ID: {} for route: {}", serviceId, routeRule.getId());
            }
        }
        //组装Filter - 合并PartFilter和RouteFilter
        List<PartFilter> ruleFilters = routeRule.getRouteFilters();
        List<RouteFilter> allRouteFilters = new ArrayList<>();
        if (ruleFilters != null) {
            allRouteFilters.addAll(ruleFilters);
        }
        if (globalFilters != null) {
            allRouteFilters.addAll(globalFilters);
        }
        new DefaultGatewayFilterChain(allRouteFilters).filter(exchange);
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

        private final Map<FilterTypeEnum, List<RouteFilter>> filterTypeEnumListMap;

        public DefaultGatewayFilterChain(List<RouteFilter> ruleFilters) {
            this.filterTypeEnumListMap = ruleFilters.stream()
                    .collect(Collectors.groupingBy(
                            RouteFilter::filterType,
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
                List<RouteFilter> filters = filterTypeEnumListMap.get(phase);
                if (Objects.nonNull(filters)) {
                    for (RouteFilter filter : filters) {
                        if (log.isDebugEnabled()) {
                            log.debug("Executing filter: {} (order: {})", filter.getClass().getSimpleName(), filter.getOrder());
                        }
                        filter.filter(exchange);
                    }
                }
            }
        }
    }

}
