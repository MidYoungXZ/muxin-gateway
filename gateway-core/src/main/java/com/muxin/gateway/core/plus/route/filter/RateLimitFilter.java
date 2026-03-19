package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RateLimitFilter extends AbstractFilter {

    public static final String TYPE = "RATE_LIMIT";

    private int requestsPerSecond;
    private int burstCapacity;

    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        this.requestsPerSecond = 1000;
        this.burstCapacity = 2000;
    }

    public RateLimitFilter(FilterDefinition definition) {
        this.name = TYPE;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
        this.requestsPerSecond = definition.getIntConfig("requests-per-second", 1000);
        this.burstCapacity = definition.getIntConfig("burst-capacity", 2000);
    }

    @Override
    protected void doFilter(HttpServerExchange exchange, FilterChain chain) {
        String clientId = getClientId(exchange);
        ClientBucket bucket = buckets.computeIfAbsent(clientId, k -> new ClientBucket(requestsPerSecond, burstCapacity));

        if (bucket.tryAcquire()) {
            chain.filter(exchange, chain);
        } else {
            logWarn("Rate limit exceeded for client: {}", clientId);
            sendRateLimitResponse(exchange);
        }
    }

    private String getClientId(HttpServerExchange exchange) {
        String clientIp = exchange.request().headers().get("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = "unknown";
        }
        return clientIp;
    }

    private void sendRateLimitResponse(HttpServerExchange exchange) {
        exchange.response().setStatus(HttpResponseStatus.TOO_MANY_REQUESTS);
        exchange.response().header("X-Rate-Limit-Limit", String.valueOf(requestsPerSecond));
        exchange.response().header("X-Rate-Limit-Remaining", "0");
        exchange.setResponseBody(String.format(
                "{\"error\":{\"code\":429,\"message\":\"Rate limit exceeded\"}}"));
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    private static class ClientBucket {
        private final AtomicInteger tokens;
        private final int maxTokens;
        private final AtomicLong lastRefillTime;

        ClientBucket(int requestsPerSecond, int burstCapacity) {
            this.maxTokens = burstCapacity;
            this.tokens = new AtomicInteger(burstCapacity);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }

        boolean tryAcquire() {
            refill();
            return tokens.decrementAndGet() >= 0;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime.get();
            if (elapsed >= 1000) {
                int refillAmount = (int) (elapsed / 1000) * maxTokens / 10;
                if (refillAmount > 0) {
                    tokens.addAndGet(refillAmount);
                    if (tokens.get() > maxTokens) {
                        tokens.set(maxTokens);
                    }
                    lastRefillTime.set(now);
                }
            }
        }
    }

    public static class Factory implements FilterFactory {

        @Override
        public Filter createFilter(FilterDefinition definition) {
            return new RateLimitFilter(definition);
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