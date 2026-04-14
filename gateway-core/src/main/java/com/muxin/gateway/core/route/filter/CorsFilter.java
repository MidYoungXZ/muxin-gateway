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
    // 标记是否需要动态反射Origin（当allowCredentials=true且allowOrigins配置为*时）
    private final boolean reflectOrigin;

    public CorsFilter(FilterDefinition definition) {
        this.allowOrigins = definition.getStringArg("allowOrigins", "*");
        this.allowMethods = definition.getStringArg("allowMethods", "*");
        this.allowHeaders = definition.getStringArg("allowHeaders", "*");
        this.allowCredentials = definition.getBooleanArg("allowCredentials", false);
        this.maxAge = definition.getIntArg("maxAge", 3600);
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();

        // CORS配置冲突处理：allowCredentials=true时不能使用通配符"*"
        // 解决方案：自动切换为反射请求Origin模式
        this.reflectOrigin = allowCredentials && "*".equals(allowOrigins);
        if (reflectOrigin) {
            log.warn("[CorsFilter] CORS配置冲突：allowCredentials=true时不能使用allowOrigins=\"*\"。" +
                    "已自动切换为动态反射Origin模式。建议配置明确的origin列表。");
        }
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }

        String origin = exchange.header("Origin");
        if (origin == null || origin.isEmpty()) {
            // 无Origin头，跳过CORS处理
            chain.doFilter(exchange);
            return;
        }

        boolean isAllowed = isOriginAllowed(origin);

        // 处理预检请求
        if (exchange.method() != null && "OPTIONS".equalsIgnoreCase(exchange.method())) {
            // 当reflectOrigin=true时，始终反射请求的Origin（不做限制）
            // 否则仅对允许的origin返回响应
            String responseOrigin = reflectOrigin ? origin : (isAllowed ? origin : null);
            if (responseOrigin != null) {
                handlePreflight(exchange, responseOrigin);
            } else {
                // 未授权的origin，返回403
                exchange.setStatus(io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN);
                exchange.setResponseBody("");
            }
            return;
        }

        // 处理实际请求
        if (isAllowed || reflectOrigin) {
            // 反射Origin模式：使用请求的Origin值（而非"*"）
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
        @Override
        public void validateConfig(FilterDefinition definition) {
            boolean credentials = definition.getBooleanArg("allowCredentials", false);
            String origins = definition.getStringArg("allowOrigins", "*");
            if (credentials && "*".equals(origins)) {
                log.warn("[CorsFilter.Factory] 配置警告：allowCredentials=true 与 allowOrigins=\"*\" 不兼容。" +
                        "将自动切换为动态反射Origin模式，建议配置明确的origin列表。");
            }
        }
    }
}
