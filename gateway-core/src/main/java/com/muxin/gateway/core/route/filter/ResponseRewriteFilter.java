package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class ResponseRewriteFilter implements Filter {

    public static final String TYPE = "ResponseRewriteFilter";

    private final Map<String, String> headersToAdd;
    private final List<String> headersToRemove;
    private final String bodyRegex;
    private final String bodyReplacement;
    private final int order;
    private final boolean enabled;

    public ResponseRewriteFilter(FilterDefinition definition) {
        Map<String, Object> args = definition.getArgs();
        this.headersToAdd = args != null ? extractHeadersToAdd(args.get("headersToAdd")) : null;
        this.headersToRemove = args != null ? extractHeadersToRemove(args.get("headersToRemove")) : null;
        this.bodyRegex = args != null ? getStringValue(args.get("bodyRegex"), null) : null;
        this.bodyReplacement = args != null ? getStringValue(args.get("bodyReplacement"), null) : null;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    private String getStringValue(Object value, String defaultValue) {
        return value != null ? value.toString() : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractHeadersToAdd(Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Map<String, String> result = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
            }
            return result;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractHeadersToRemove(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                    .filter(v -> v != null)
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.toList());
        }
        return null;
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }

        chain.doFilter(exchange);

        if (headersToRemove != null) {
            for (String header : headersToRemove) {
                exchange.responseHeaders().remove(header);
                if (log.isDebugEnabled()) {
                    log.debug("[ResponseRewriteFilter] 移除响应头: {}", header);
                }
            }
        }

        if (headersToAdd != null) {
            for (Map.Entry<String, String> entry : headersToAdd.entrySet()) {
                exchange.setResponseHeader(entry.getKey(), entry.getValue());
                if (log.isDebugEnabled()) {
                    log.debug("[ResponseRewriteFilter] 添加响应头: {} = {}", entry.getKey(), entry.getValue());
                }
            }
        }

        if (bodyRegex != null && bodyReplacement != null) {
            String body = exchange.getResponseBody();
            if (body != null && !body.isEmpty()) {
                String newBody = body.replaceAll(bodyRegex, bodyReplacement);
                exchange.setResponseBody(newBody);
                if (log.isDebugEnabled()) {
                    log.debug("[ResponseRewriteFilter] Body 重写完成");
                }
            }
        }
    }

    @Override
    public String getName() {
        return TYPE;
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
            return new ResponseRewriteFilter(definition);
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