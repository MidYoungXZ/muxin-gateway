package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RemoveRequestHeaderFilter implements Filter {

    public static final String TYPE = "RemoveRequestHeader";

    private final String name;
    private final int order;
    private final boolean enabled;

    public RemoveRequestHeaderFilter(String name) {
        this(name, 0, true);
    }

    public RemoveRequestHeaderFilter(String name, int order, boolean enabled) {
        this.name = name;
        this.order = order;
        this.enabled = enabled;
    }

    public RemoveRequestHeaderFilter(FilterDefinition definition) {
        Map<String, Object> args = definition.getArgs();
        this.name = args != null ? (String) args.get("name") : null;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled || name == null || name.isEmpty()) {
            chain.doFilter(exchange);
            return;
        }

        exchange.removeHeader(name);

        if (log.isDebugEnabled()) {
            log.debug("[RemoveRequestHeaderFilter] 移除请求头: {}", name);
        }

        chain.doFilter(exchange);
    }

    @Override
    public String getName() {
        return "RemoveRequestHeaderFilter-" + name;
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public static class Factory implements FilterFactory {

        @Override
        public Filter createFilter(FilterDefinition definition) {
            return new RemoveRequestHeaderFilter(definition);
        }

        @Override
        public String getSupportedFilterName() {
            return TYPE;
        }

        @Override
        public void validateConfig(FilterDefinition definition) {
            Map<String, Object> args = definition.getArgs();
            if (args == null || !args.containsKey("name")) {
                throw new IllegalArgumentException("RemoveRequestHeaderFilter 必须配置 name 参数");
            }
        }
    }
}