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

    private final int replenishRate;
    private final int burstCapacity;
    private final int order;
    private final boolean enabled;

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RequestRateLimiterFilter(int replenishRate, int burstCapacity) {
        this(replenishRate, burstCapacity, 0, true);
    }

    public RequestRateLimiterFilter(int replenishRate, int burstCapacity, int order, boolean enabled) {
        this.replenishRate = replenishRate;
        this.burstCapacity = burstCapacity;
        this.order = order;
        this.enabled = enabled;
    }

    public RequestRateLimiterFilter(FilterDefinition definition) {
        Map<String, Object> config = definition.getConfig();
        this.replenishRate = config != null ? getIntValue(config.get("replenishRate"), 10) : 10;
        this.burstCapacity = config != null ? getIntValue(config.get("burstCapacity"), 20) : 20;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    private int getIntValue(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }

        String key = getClientKey(exchange);
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(burstCapacity, replenishRate));

        if (bucket.tryAcquire()) {
            if (log.isDebugEnabled()) {
                log.debug("[RequestRateLimiterFilter] 请求通过, key: {}, available tokens: {}", key, bucket.availableTokens());
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

    @Override
    public String getName() {
        return "RequestRateLimiterFilter";
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
                long newTokens = Math.min(capacity, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }

        long availableTokens() {
            refill();
            return tokens.get();
        }
    }

    public static class Factory implements FilterFactory {

        @Override
        public Filter createFilter(FilterDefinition definition) {
            return new RequestRateLimiterFilter(definition);
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