package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.util.UUID;

public class RequestIdFilter extends AbstractFilter {

    public static final String DEFAULT_HEADER_NAME = "X-Request-ID";
    public static final String TYPE = "REQUEST_ID";

    private String headerName;
    private boolean generateIfMissing;

    public RequestIdFilter() {
        this.headerName = DEFAULT_HEADER_NAME;
        this.generateIfMissing = true;
    }

    public RequestIdFilter(String headerName, boolean generateIfMissing) {
        this.headerName = headerName != null ? headerName : DEFAULT_HEADER_NAME;
        this.generateIfMissing = generateIfMissing;
    }

    public RequestIdFilter(FilterDefinition definition) {
        this.name = TYPE;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
        this.headerName = definition.getStringConfig("header-name", DEFAULT_HEADER_NAME);
        this.generateIfMissing = definition.getBooleanConfig("generate-if-missing", true);
    }

    @Override
    protected void doFilter(HttpServerExchange exchange, FilterChain chain) {
        String requestId = exchange.request().headers().get(headerName);

        if (requestId == null || requestId.isEmpty()) {
            if (generateIfMissing) {
                requestId = generateRequestId();
                exchange.request().header(headerName, requestId);
                logDebug("Generated request ID: {}", requestId);
            }
        } else {
            logDebug("Using existing request ID: {}", requestId);
        }

        exchange.request().header("X-Request-ID", requestId);
        chain.doFilter(exchange);
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    public static class Factory implements FilterFactory {

        @Override
        public Filter createFilter(FilterDefinition definition) {
            return new RequestIdFilter(definition);
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