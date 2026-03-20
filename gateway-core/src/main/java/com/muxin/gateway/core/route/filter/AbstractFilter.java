package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFilter implements Filter {

    protected String name;
    protected int order;
    protected boolean enabled = true;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }
        doFilter(exchange, chain);
    }

    protected abstract void doFilter(HttpServerExchange exchange, FilterChain chain);

    protected void logDebug(String message, Object... args) {
        if (log.isDebugEnabled()) {
            if (args == null || args.length == 0) {
                log.debug("[{}] {}", name, message);
            } else {
                Object[] combined = new Object[args.length + 1];
                combined[0] = name;
                System.arraycopy(args, 0, combined, 1, args.length);
                log.debug("[{}] " + message, combined);
            }
        }
    }

    protected void logInfo(String message, Object... args) {
        if (log.isInfoEnabled()) {
            if (args == null || args.length == 0) {
                log.info("[{}] {}", name, message);
            } else {
                Object[] combined = new Object[args.length + 1];
                combined[0] = name;
                System.arraycopy(args, 0, combined, 1, args.length);
                log.info("[{}] " + message, combined);
            }
        }
    }

    protected void logWarn(String message, Object... args) {
        if (args == null || args.length == 0) {
            log.warn("[{}] {}", name, message);
        } else {
            Object[] combined = new Object[args.length + 1];
            combined[0] = name;
            System.arraycopy(args, 0, combined, 1, args.length);
            log.warn("[{}] " + message, combined);
        }
    }

    protected void logError(String message, Object... args) {
        if (args == null || args.length == 0) {
            log.error("[{}] {}", name, message);
        } else {
            Object[] combined = new Object[args.length + 1];
            combined[0] = name;
            System.arraycopy(args, 0, combined, 1, args.length);
            log.error("[{}] " + message, combined);
        }
    }
}