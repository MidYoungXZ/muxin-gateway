package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AddResponseHeaderFilter implements Filter {

    public static final String TYPE = "AddResponseHeader";

    private final String name;
    private final String value;
    private final int order;
    private final boolean enabled;

    public AddResponseHeaderFilter(String name, String value) {
        this(name, value, 0, true);
    }

    public AddResponseHeaderFilter(String name, String value, int order, boolean enabled) {
        this.name = name;
        this.value = value;
        this.order = order;
        this.enabled = enabled;
    }

    public AddResponseHeaderFilter(FilterDefinition definition) {
        Map<String, Object> config = definition.getConfig();
        this.name = config != null ? (String) config.get("name") : null;
        this.value = config != null ? (String) config.get("value") : null;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        chain.doFilter(exchange);

        if (!enabled || name == null || name.isEmpty()) {
            return;
        }

        String actualValue = resolveValue(value);
        exchange.setResponseHeader(name, actualValue);

        if (log.isDebugEnabled()) {
            log.debug("[AddResponseHeaderFilter] 添加响应头: {} = {}", name, actualValue);
        }
    }

    private String resolveValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    public String getName() {
        return "AddResponseHeaderFilter-" + name;
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
            return new AddResponseHeaderFilter(definition);
        }

        @Override
        public String getSupportedFilterName() {
            return TYPE;
        }

        @Override
        public void validateConfig(FilterDefinition definition) {
            Map<String, Object> config = definition.getConfig();
            if (config == null || !config.containsKey("name")) {
                throw new IllegalArgumentException("AddResponseHeaderFilter 必须配置 name 参数");
            }
        }
    }
}