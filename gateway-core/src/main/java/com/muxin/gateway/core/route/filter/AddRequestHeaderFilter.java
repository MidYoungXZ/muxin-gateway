package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AddRequestHeaderFilter implements Filter {

    public static final String TYPE = "AddRequestHeader";

    private final String name;
    private final String value;
    private final int order;
    private final boolean enabled;

    public AddRequestHeaderFilter(String name, String value) {
        this(name, value, 0, true);
    }

    public AddRequestHeaderFilter(String name, String value, int order, boolean enabled) {
        this.name = name;
        this.value = value;
        this.order = order;
        this.enabled = enabled;
    }

    public AddRequestHeaderFilter(FilterDefinition definition) {
        Map<String, Object> config = definition.getConfig();
        this.name = config != null ? (String) config.get("name") : null;
        this.value = config != null ? (String) config.get("value") : null;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled || name == null || name.isEmpty()) {
            chain.doFilter(exchange);
            return;
        }

        String actualValue = resolveValue(value);
        exchange.header(name, actualValue);

        if (log.isDebugEnabled()) {
            log.debug("[AddRequestHeaderFilter] 添加请求头: {} = {}", name, actualValue);
        }

        chain.doFilter(exchange);
    }

    private String resolveValue(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("#{T(System).currentTimeMillis()}")) {
            return value.replace("#{T(System).currentTimeMillis()}", String.valueOf(System.currentTimeMillis()));
        }
        if (value.contains("#{T(java.util.UUID).randomUUID().toString()}")) {
            return value.replace("#{T(java.util.UUID).randomUUID().toString()}", java.util.UUID.randomUUID().toString());
        }
        return value;
    }

    @Override
    public String getName() {
        return "AddRequestHeaderFilter-" + name;
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
            return new AddRequestHeaderFilter(definition);
        }

        @Override
        public String getSupportedFilterName() {
            return TYPE;
        }

        @Override
        public void validateConfig(FilterDefinition definition) {
            Map<String, Object> config = definition.getConfig();
            if (config == null || !config.containsKey("name")) {
                throw new IllegalArgumentException("AddRequestHeaderFilter 必须配置 name 参数");
            }
        }
    }
}