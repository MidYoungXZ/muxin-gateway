package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RewritePathFilter implements Filter {

    public static final String TYPE = "RewritePath";

    private final String regexp;
    private final String replacement;
    private final int order;
    private final boolean enabled;
    private final Pattern pattern;

    public RewritePathFilter(String regexp, String replacement) {
        this(regexp, replacement, 0, true);
    }

    public RewritePathFilter(String regexp, String replacement, int order, boolean enabled) {
        this.regexp = regexp;
        this.replacement = replacement;
        this.order = order;
        this.enabled = enabled;
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
    }

    public RewritePathFilter(FilterDefinition definition) {
        Map<String, Object> args = definition.getArgs();
        this.regexp = args != null ? (String) args.get("regexp") : null;
        this.replacement = args != null ? (String) args.get("replacement") : null;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
        this.pattern = regexp != null && !regexp.isEmpty() ? Pattern.compile(regexp) : null;
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled || pattern == null || replacement == null) {
            chain.doFilter(exchange);
            return;
        }

        String originalPath = exchange.fullPath();
        if (originalPath == null) {
            chain.doFilter(exchange);
            return;
        }

        Matcher matcher = pattern.matcher(originalPath);
        if (matcher.matches()) {
            String newPath = replacement;
            
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
                log.debug("[RewritePathFilter] 路径重写: {} -> {}", originalPath, finalUri);
            }
        }

        chain.doFilter(exchange);
    }

    @Override
    public String getName() {
        return "RewritePathFilter";
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
            return new RewritePathFilter(definition);
        }

        @Override
        public String getSupportedFilterName() {
            return TYPE;
        }

        @Override
        public void validateConfig(FilterDefinition definition) {
            Map<String, Object> args = definition.getArgs();
            if (args == null) {
                throw new IllegalArgumentException("RewritePathFilter 必须配置参数");
            }
        }
    }
}