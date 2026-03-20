package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.exchange.HttpServerExchange;
import com.muxin.gateway.core.plus.route.DefaultRoute;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.predicate.PathPredicate;
import lombok.extern.slf4j.Slf4j;

/**
 * 路径重写过滤器
 * 负责剥离请求路径的前缀
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class PathRewriteFilter implements Filter {

    public static final String TYPE = "PATH_REWRITE";
    public static final String NAME = "PathRewriteFilter";

    private final int order;
    private final boolean enabled;

    public PathRewriteFilter() {
        this(0, true);
    }

    public PathRewriteFilter(int order, boolean enabled) {
        this.order = order;
        this.enabled = enabled;
    }

    public PathRewriteFilter(FilterDefinition definition) {
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }

        Route route = (Route) exchange.getAttribute("matchedRoute");
        if (route == null) {
            log.warn("[PathRewriteFilter] 未找到匹配的路由，跳过路径重写");
            chain.doFilter(exchange);
            return;
        }

        if (route instanceof DefaultRoute defaultRoute) {
            int stripPrefixCount = defaultRoute.getStripPrefixCount();
            if (stripPrefixCount > 0) {
                PathPredicate pathPredicate = defaultRoute.getPathPredicate();
                String originalPath = exchange.fullPath();
                String strippedPath = pathPredicate.stripPrefix(originalPath);

                exchange.setAttribute("stripPrefixCount", stripPrefixCount);
                exchange.setAttribute("strippedPath", strippedPath);

                if (log.isDebugEnabled()) {
                    log.debug("[PathRewriteFilter] 路径前缀剥离: {} -> {} (剥离{}段)",
                            originalPath, strippedPath, stripPrefixCount);
                }
            }
        }

        chain.doFilter(exchange);
    }

    @Override
    public String getName() {
        return NAME;
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
            return new PathRewriteFilter(definition);
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
