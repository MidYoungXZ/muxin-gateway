package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestLogFilter extends AbstractFilter {

    public static final String TYPE = "REQUEST_LOG";

    private boolean includeBody;
    private boolean includeHeaders;

    public RequestLogFilter() {
        this.includeBody = false;
        this.includeHeaders = false;
    }

    public RequestLogFilter(boolean includeBody, boolean includeHeaders) {
        this.includeBody = includeBody;
        this.includeHeaders = includeHeaders;
    }

    public RequestLogFilter(FilterDefinition definition) {
        this.name = TYPE;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
        this.includeBody = definition.getBooleanConfig("include-body", false);
        this.includeHeaders = definition.getBooleanConfig("include-headers", false);
    }

    @Override
    protected void doFilter(HttpServerExchange exchange, FilterChain chain) {
        long startTime = System.currentTimeMillis();

        String method = exchange.method();
        String path = exchange.uri();
        String requestId = exchange.requestId();

        logInfo(">> {} {} {}", requestId, method, path);

        if (includeHeaders) {
            logHeaders(exchange);
        }

        if (includeBody && exchange.getRequestBody() != null) {
            String body = exchange.getRequestBody();
            if (body.length() > 500) {
                body = body.substring(0, 500) + "...(truncated)";
            }
            logInfo(">> Body: {}", body);
        }

        try {
            chain.doFilter(exchange);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String statusInfo;
            if (exchange.hasResponse() && exchange.status() != null) {
                int statusCode = exchange.status().code();
                statusInfo = statusCode + " (" + duration + "ms)";
            } else {
                statusInfo = "no-response (" + duration + "ms)";
            }
            logInfo("<< {} {} {} - {}", requestId, method, path, statusInfo);
        }
    }

    private void logHeaders(HttpServerExchange exchange) {
        io.netty.handler.codec.http.HttpHeaders headers = exchange.headers();
        for (String name : headers.names()) {
            logInfo(">> {}: {}", name, headers.get(name));
        }
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    public static class Factory implements FilterFactory {

        @Override
        public Filter createFilter(FilterDefinition definition) {
            return new RequestLogFilter(definition);
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