package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CircuitBreakerFilter implements Filter {

    public static final String TYPE = "CircuitBreaker";

    private static final int STATE_CLOSED = 0;
    private static final int STATE_OPEN = 1;
    private static final int STATE_HALF_OPEN = 2;

    private static final long CLEANUP_INTERVAL_MS = 30 * 60 * 1000L;
    private static final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    private static volatile long lastCleanupTime = System.currentTimeMillis();

    private final String name;
    private final String fallbackUri;
    private final int failureRateThreshold;
    private final int ringBufferSize;
    private final long waitDurationInOpenState;
    private final int order;
    private final boolean enabled;

    public CircuitBreakerFilter(FilterDefinition definition) {
        this.name = definition.getStringArg("name", "default");
        this.fallbackUri = definition.getStringArg("fallbackUri", "/fallback");
        this.failureRateThreshold = definition.getIntArg("failureRateThreshold", 50);
        this.ringBufferSize = definition.getIntArg("ringBufferSize", 100);
        this.waitDurationInOpenState = definition.getLongArg("waitDurationInOpenState", 60000);
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
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            lastCleanupTime = now;
            circuitBreakers.clear();
        }

        CircuitBreakerState state = circuitBreakers.computeIfAbsent(name,
                k -> new CircuitBreakerState(failureRateThreshold, ringBufferSize, waitDurationInOpenState));

        if (state.getState() == STATE_OPEN) {
            if (state.tryTransitionToHalfOpen()) {
                log.info("[CircuitBreakerFilter] 熔断器 {} 从 OPEN 转换到 HALF_OPEN", name);
            } else {
                handleFallback(exchange);
                return;
            }
        }

        try {
            chain.doFilter(exchange);
            HttpResponseStatus status = exchange.status();
            if (status != null && status.code() >= 500) {
                state.recordFailure();
            } else {
                state.recordSuccess();
            }
        } catch (Exception e) {
            state.recordFailure();
            log.error("[CircuitBreakerFilter] 请求异常: {}", e.getMessage());
            handleFallback(exchange);
        }
    }

    private void handleFallback(HttpServerExchange exchange) {
        log.warn("[CircuitBreakerFilter] 触发熔断, 执行降级: {}", fallbackUri);
        exchange.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
        exchange.setResponseHeader("Content-Type", "application/json");
        exchange.setResponseBody("{\"code\":503,\"message\":\"Service Unavailable - Circuit Breaker Open\"}");
    }

    @Override public String getName() { return TYPE + "-" + name; }
    @Override public FilterType getType() { return FilterType.PRE; }
    @Override public int getOrder() { return order; }
    @Override public boolean isEnabled() { return enabled; }

    private static class CircuitBreakerState {
        private final int failureRateThreshold;
        private final int ringBufferSize;
        private final long waitDurationInOpenState;

        private volatile int state = STATE_CLOSED;
        private volatile long lastFailureTime = 0;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger totalCount = new AtomicInteger(0);

        CircuitBreakerState(int failureRateThreshold, int ringBufferSize, long waitDurationInOpenState) {
            this.failureRateThreshold = failureRateThreshold;
            this.ringBufferSize = ringBufferSize;
            this.waitDurationInOpenState = waitDurationInOpenState;
        }

        int getState() { return state; }

        synchronized boolean tryTransitionToHalfOpen() {
            if (state == STATE_OPEN && System.currentTimeMillis() - lastFailureTime >= waitDurationInOpenState) {
                state = STATE_HALF_OPEN;
                return true;
            }
            return false;
        }

        void recordFailure() {
            lastFailureTime = System.currentTimeMillis();
            failureCount.incrementAndGet();
            int total = totalCount.incrementAndGet();

            if (total >= ringBufferSize) {
                synchronized (this) {
                    double failureRate = (failureCount.get() * 100.0) / totalCount.get();
                    if (failureRate >= failureRateThreshold && state != STATE_OPEN) {
                        state = STATE_OPEN;
                        log.warn("CircuitBreaker OPEN - failure rate: {}%", failureRate);
                    }
                }
            }
        }

        void recordSuccess() {
            totalCount.incrementAndGet();
            if (state == STATE_HALF_OPEN) {
                synchronized (this) {
                    if (state == STATE_HALF_OPEN) {
                        state = STATE_CLOSED;
                        failureCount.set(0);
                        totalCount.set(0);
                        log.info("CircuitBreaker CLOSED - service recovered");
                    }
                }
            }
        }
    }

    public static class Factory implements FilterFactory {
        @Override public Filter createFilter(FilterDefinition definition) { return new CircuitBreakerFilter(definition); }
        @Override public String getSupportedFilterName() { return TYPE; }
        @Override public void validateConfig(FilterDefinition definition) {}
    }
}
