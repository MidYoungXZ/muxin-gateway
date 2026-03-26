package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RemoveResponseHeaderFilter implements Filter {

    public static final String TYPE = "RemoveResponseHeader";

    private final String name;
    private final int order;
    private final boolean enabled;

    public RemoveResponseHeaderFilter(String name) {
        this(name, 0, true);
    }

    public RemoveResponseHeaderFilter(String name, int order, boolean enabled) {
        this.name = name;
        this.order = order;
        this.enabled = enabled;
    }

    public RemoveResponseHeaderFilter(FilterDefinition definition) {
        Map<String, Object> config = definition.getConfig();
        this.name = config != null ? (String) config.get("name") : null;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        chain.doFilter(exchange);

        if (!enabled || name == null || name.isEmpty()) {
            return;
        }

        HttpHeaders responseHeaders = exchange.responseHeaders();
        if (responseHeaders != null) {
            responseHeaders.remove(name);
        }

        if (log.isDebugEnabled()) {
            log.debug("[RemoveResponseHeaderFilter] 移除响应头: {}", name);
        }
    }

    @Override
    public String getName() {
        return "RemoveResponseHeaderFilter-" + name;
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
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
            return new RemoveResponseHeaderFilter(definition);
        }

        @Override
        public String getSupportedFilterName() {
            return TYPE;
        }

        @Override
        public void validateConfig(FilterDefinition definition) {
            Map<String, Object> config = definition.getConfig();
            if (config == null || !config.containsKey("name")) {
                throw new IllegalArgumentException("RemoveResponseHeaderFilter 必须配置 name 参数");
            }
        }
    }
}