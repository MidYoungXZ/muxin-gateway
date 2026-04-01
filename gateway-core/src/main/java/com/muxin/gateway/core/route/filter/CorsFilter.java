package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class CorsFilter implements Filter {

    public static final String TYPE = "CorsFilter";

    private final String allowOrigins;
    private final String allowMethods;
    private final String allowHeaders;
    private final boolean allowCredentials;
    private final int maxAge;
    private final int order;
    private final boolean enabled;

    public CorsFilter(FilterDefinition definition) {
        Map<String, Object> args = definition.getArgs();
        this.allowOrigins = args != null ? getStringValue(args.get("allowOrigins"), "*") : "*";
        this.allowMethods = args != null ? getStringValue(args.get("allowMethods"), "*") : "*";
        this.allowHeaders = args != null ? getStringValue(args.get("allowHeaders"), "*") : "*";
        this.allowCredentials = args != null ? getBooleanValue(args.get("allowCredentials"), false) : false;
        this.maxAge = args != null ? getIntValue(args.get("maxAge"), 3600) : 3600;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    private String getStringValue(Object value, String defaultValue) {
        return value != null ? value.toString() : defaultValue;
    }

    private int getIntValue(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBooleanValue(Object value, boolean defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }

        String origin = exchange.header("Origin");
        if (origin == null || origin.isEmpty()) {
            origin = "*";
        }

        boolean isAllowed = isOriginAllowed(origin);

        if (exchange.method() != null && "OPTIONS".equalsIgnoreCase(exchange.method())) {
            handlePreflight(exchange, isAllowed ? origin : allowOrigins);
            return;
        }

        if (isAllowed) {
            exchange.setResponseHeader("Access-Control-Allow-Origin", origin);
            if (allowCredentials) {
                exchange.setResponseHeader("Access-Control-Allow-Credentials", "true");
            }
            if (!"*".equals(allowHeaders)) {
                exchange.setResponseHeader("Access-Control-Expose-Headers", allowHeaders);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("[CorsFilter] CORS 处理完成, origin: {}, allowed: {}", origin, isAllowed);
        }

        chain.doFilter(exchange);
    }

    private boolean isOriginAllowed(String origin) {
        if ("*".equals(allowOrigins)) {
            return true;
        }
        String[] allowedOrigins = allowOrigins.split(",");
        for (String allowed : allowedOrigins) {
            if (allowed.trim().equalsIgnoreCase(origin)) {
                return true;
            }
        }
        return false;
    }

    private void handlePreflight(HttpServerExchange exchange, String origin) {
        exchange.setResponseHeader("Access-Control-Allow-Origin", origin);
        exchange.setResponseHeader("Access-Control-Allow-Methods", allowMethods);
        exchange.setResponseHeader("Access-Control-Allow-Headers", allowHeaders);
        exchange.setResponseHeader("Access-Control-Max-Age", String.valueOf(maxAge));
        if (allowCredentials) {
            exchange.setResponseHeader("Access-Control-Allow-Credentials", "true");
        }
        exchange.setStatus(io.netty.handler.codec.http.HttpResponseStatus.OK);
        exchange.setResponseBody("");

        if (log.isDebugEnabled()) {
            log.debug("[CorsFilter] 预检请求处理完成, origin: {}", origin);
        }
    }

    @Override
    public String getName() {
        return TYPE;
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
            return new CorsFilter(definition);
        }

        @Override
        public String getSupportedFilterName() {
            return TYPE;
        }

        @Override
        public void validateConfig(FilterDefinition definition) {
        }
    }
}