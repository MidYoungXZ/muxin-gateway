package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RequestRateLimiterFilter implements Filter {

    public static final String TYPE = "RequestRateLimiter";

    private static final long BUCKET_TTL_MS = 30 * 60 * 1000L;

    private final int replenishRate;
    private final int burstCapacity;
    private final int order;
    private final boolean enabled;

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();

    public RequestRateLimiterFilter(FilterDefinition definition) {
        this.replenishRate = definition.getIntArg("replenishRate", 10);
        this.burstCapacity = definition.getIntArg("burstCapacity", 20);
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastCleanupTime > BUCKET_TTL_MS) {
            lastCleanupTime = now;
            buckets.clear();
        }

        String key = getClientKey(exchange);
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(burstCapacity, replenishRate));

        if (bucket.tryAcquire()) {
            if (log.isDebugEnabled()) {
                log.debug("[RequestRateLimiterFilter] 请求通过, key: {}", key);
            }
            chain.doFilter(exchange);
        } else {
            log.warn("[RequestRateLimiterFilter] 请求被限流, key: {}", key);
            exchange.setStatus(HttpResponseStatus.TOO_MANY_REQUESTS);
            exchange.setResponseHeader("Content-Type", "application/json");
            exchange.setResponseBody("{\"code\":429,\"message\":\"Too Many Requests\"}");
        }
    }

    private String getClientKey(HttpServerExchange exchange) {
        String xForwardedFor = exchange.header("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String realIp = exchange.header("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return "default";
    }

    @Override public String getName() { return TYPE; }
    @Override public FilterType getType() { return FilterType.PRE; }
    @Override public int getOrder() { return order; }
    @Override public boolean isEnabled() { return enabled; }

    private static class TokenBucket {
        private final int capacity;
        private final int refillRate;
        private final AtomicLong tokens;
        private volatile long lastRefillTime;

        TokenBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed >= 1000) {
                long tokensToAdd = (elapsed / 1000) * refillRate;
                tokens.set(Math.min(capacity, tokens.get() + tokensToAdd));
                lastRefillTime = now;
            }
        }
    }

    public static class Factory implements FilterFactory {
        @Override public Filter createFilter(FilterDefinition definition) { return new RequestRateLimiterFilter(definition); }
        @Override public String getSupportedFilterName() { return TYPE; }
        @Override public void validateConfig(FilterDefinition definition) {}
    }
}
