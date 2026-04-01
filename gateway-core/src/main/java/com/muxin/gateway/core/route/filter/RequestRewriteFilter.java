package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RequestRewriteFilter implements Filter {

    public static final String TYPE = "RequestRewriteFilter";

    private final String pathRegex;
    private final String pathReplacement;
    private final Map<String, String> headersToAdd;
    private final List<String> headersToRemove;
    private final int order;
    private final boolean enabled;
    private final Pattern pattern;

    public RequestRewriteFilter(FilterDefinition definition) {
        Map<String, Object> args = definition.getArgs();
        this.pathRegex = args != null ? getStringValue(args.get("pathRegex"), null) : null;
        this.pathReplacement = args != null ? getStringValue(args.get("pathReplacement"), null) : null;
        this.headersToAdd = args != null ? extractHeadersToAdd(args.get("headersToAdd")) : null;
        this.headersToRemove = args != null ? extractHeadersToRemove(args.get("headersToRemove")) : null;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
        this.pattern = pathRegex != null && !pathRegex.isEmpty() ? Pattern.compile(pathRegex) : null;
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

        if (pattern != null && pathReplacement != null) {
            String originalPath = exchange.fullPath();
            if (originalPath != null) {
                Matcher matcher = pattern.matcher(originalPath);
                if (matcher.matches()) {
                    String newPath = pathReplacement;
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String groupValue = matcher.group(i);
                        if (groupValue != null) {
                            newPath = newPath.replace("${segment}", groupValue);
                            newPath = newPath.replace("$" + i, groupValue);
                        }
                    }
                    if (!newPath.startsWith("/")) {
                        newPath = "/" + newPath;
                    }
                    String queryString = "";
                    int queryIndex = originalPath.indexOf('?');
                    if (queryIndex > 0) {
                        queryString = originalPath.substring(queryIndex);
                    }
                    String finalUri = newPath + queryString;
                    exchange.uri(finalUri);
                    exchange.setAttribute("originalPath", originalPath);
                    exchange.setAttribute("rewrittenPath", finalUri);
                    if (log.isDebugEnabled()) {
                        log.debug("[RequestRewriteFilter] 路径重写: {} -> {}", originalPath, finalUri);
                    }
                }
            }
        }

        if (headersToRemove != null) {
            for (String header : headersToRemove) {
                exchange.removeHeader(header);
                if (log.isDebugEnabled()) {
                    log.debug("[RequestRewriteFilter] 移除请求头: {}", header);
                }
            }
        }

        if (headersToAdd != null) {
            for (Map.Entry<String, String> entry : headersToAdd.entrySet()) {
                String resolvedValue = resolveValue(entry.getValue());
                exchange.header(entry.getKey(), resolvedValue);
                if (log.isDebugEnabled()) {
                    log.debug("[RequestRewriteFilter] 添加请求头: {} = {}", entry.getKey(), resolvedValue);
                }
            }
        }

        chain.doFilter(exchange);
    }

    private String resolveValue(String value) {
        if (value == null) return "";
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
            return new RequestRewriteFilter(definition);
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