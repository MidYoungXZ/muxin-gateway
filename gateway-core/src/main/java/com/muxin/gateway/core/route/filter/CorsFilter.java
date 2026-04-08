package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

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
        this.allowOrigins = definition.getStringArg("allowOrigins", "*");
        this.allowMethods = definition.getStringArg("allowMethods", "*");
        this.allowHeaders = definition.getStringArg("allowHeaders", "*");
        this.allowCredentials = definition.getBooleanArg("allowCredentials", false);
        this.maxAge = definition.getIntArg("maxAge", 3600);
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
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

        chain.doFilter(exchange);
    }

    private boolean isOriginAllowed(String origin) {
        if ("*".equals(allowOrigins)) return true;
        for (String allowed : allowOrigins.split(",")) {
            if (allowed.trim().equalsIgnoreCase(origin)) return true;
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
    }

    @Override public String getName() { return TYPE; }
    @Override public FilterType getType() { return FilterType.PRE; }
    @Override public int getOrder() { return order; }
    @Override public boolean isEnabled() { return enabled; }

    public static class Factory implements FilterFactory {
        @Override public Filter createFilter(FilterDefinition definition) { return new CorsFilter(definition); }
        @Override public String getSupportedFilterName() { return TYPE; }
        @Override public void validateConfig(FilterDefinition definition) {}
    }
}
