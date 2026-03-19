package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CorsFilter extends AbstractFilter {

    public static final String TYPE = "CORS";

    private Set<String> allowedOrigins;
    private Set<String> allowedMethods;
    private Set<String> allowedHeaders;
    private boolean allowCredentials;
    private int maxAge;

    public CorsFilter() {
        this.allowedOrigins = new HashSet<>(Arrays.asList("*"));
        this.allowedMethods = new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        this.allowedHeaders = new HashSet<>(Arrays.asList("*"));
        this.allowCredentials = true;
        this.maxAge = 3600;
    }

    public CorsFilter(FilterDefinition definition) {
        this.name = TYPE;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();

        Object allowedOriginsObj = definition.getConfigValue("allowed-origins");
        if (allowedOriginsObj instanceof java.util.List) {
            this.allowedOrigins = new HashSet<>((java.util.List<String>) allowedOriginsObj);
        } else {
            this.allowedOrigins = new HashSet<>(Arrays.asList("*"));
        }

        Object allowedMethodsObj = definition.getConfigValue("allowed-methods");
        if (allowedMethodsObj instanceof java.util.List) {
            this.allowedMethods = new HashSet<>((java.util.List<String>) allowedMethodsObj);
        } else {
            this.allowedMethods = new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }

        Object allowedHeadersObj = definition.getConfigValue("allowed-headers");
        if (allowedHeadersObj instanceof java.util.List) {
            this.allowedHeaders = new HashSet<>((java.util.List<String>) allowedHeadersObj);
        } else {
            this.allowedHeaders = new HashSet<>(Arrays.asList("*"));
        }

        this.allowCredentials = definition.getBooleanConfig("allow-credentials", true);
        this.maxAge = definition.getIntConfig("max-age", 3600);
    }

    @Override
    protected void doFilter(HttpServerExchange exchange, FilterChain chain) {
        io.netty.handler.codec.http.HttpHeaders headers = exchange.request().headers();
        String origin = headers.get(HttpHeaderNames.ORIGIN);

        if (origin == null) {
            origin = headers.get(HttpHeaderNames.REFERER);
        }

        if (origin != null && isOriginAllowed(origin)) {
            exchange.response().header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), origin);
            if (allowCredentials) {
                exchange.response().header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true");
            }
            exchange.response().header(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString(), String.valueOf(maxAge));
        }

        if ("OPTIONS".equalsIgnoreCase(exchange.request().method().name())) {
            if (!allowedOrigins.isEmpty() && !allowedOrigins.contains("*")) {
                String allowedMethodsStr = String.join(", ", allowedMethods);
                exchange.response().header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), allowedMethodsStr);

                if (!allowedHeaders.isEmpty() && !allowedHeaders.contains("*")) {
                    String allowedHeadersStr = String.join(", ", allowedHeaders);
                    exchange.response().header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(), allowedHeadersStr);
                }
            }
            exchange.response().setStatus(io.netty.handler.codec.http.HttpResponseStatus.OK);
            logDebug("CORS preflight handled for origin: {}", origin);
            return;
        }

        chain.filter(exchange, chain);
    }

    private boolean isOriginAllowed(String origin) {
        if (allowedOrigins.contains("*")) {
            return true;
        }
        return allowedOrigins.contains(origin);
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
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