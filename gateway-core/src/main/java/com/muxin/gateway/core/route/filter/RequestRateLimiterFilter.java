package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RequestRateLimiterFilter implements Filter {

    public static final String TYPE = "RequestRateLimiter";

    private static final long BUCKET_TTL_MS = 30 * 60 * 1000L;
    private static final String RATE_LIMIT_RESPONSE = "{\"code\":429,\"message\":\"Too Many Requests\"}";

    // 可信代理IP列表（默认为空，需要配置）
    private static final Set<String> TRUSTED_PROXIES = Collections.emptySet();

    private final int replenishRate;
    private final int burstCapacity;
    private final int order;
    private final boolean enabled;

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupTime = new AtomicLong(System.currentTimeMillis());

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
        long lastCleanup = lastCleanupTime.get();
        if (now - lastCleanup > BUCKET_TTL_MS) {
            if (lastCleanupTime.compareAndSet(lastCleanup, now)) {
                buckets.clear();
            }
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
            exchange.setResponseBody(RATE_LIMIT_RESPONSE);
        }
    }

    private String getClientKey(HttpServerExchange exchange) {
        String xForwardedFor = exchange.header("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String[] ips = xForwardedFor.split(",");
            for (int i = ips.length - 1; i >= 0; i--) {
                String ip = ips[i].trim();
                if (!ip.isEmpty() && !isTrustedProxy(ip)) {
                    return ip;
                }
            }
            if (ips.length > 0) {
                return ips[ips.length - 1].trim();
            }
        }

        String realIp = exchange.header("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp.trim();
        }

        String remoteAddress = exchange.remoteAddress();
        if (remoteAddress != null && !remoteAddress.isEmpty()) {
            return remoteAddress;
        }

        return "default-client";
    }

    /**
     * 判断是否为可信代理IP
     * TODO: 需要从配置加载可信代理列表
     */
    private boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        if (TRUSTED_PROXIES.contains(ip)) {
            return true;
        }
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
                    return true;
                }
                return false;
            }
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            if (first == 10) return true;
            if (first == 192 && second == 168) return true;
            if (first == 127) return true;
            if (first == 172 && second >= 16 && second <= 31) return true;
            return false;
        } catch (NumberFormatException e) {
            return ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
        }
    }

    @Override public String getName() { return TYPE; }
    @Override public FilterType getType() { return FilterType.PRE; }
    @Override public int getOrder() { return order; }
    @Override public boolean isEnabled() { return enabled; }

    /**
     * 无锁令牌桶实现
     * 使用AtomicLong的CAS操作替代synchronized，提升高并发性能
     */
    private static class TokenBucket {
        private final int capacity;
        private final int refillRate;
        private final AtomicLong tokensAndTime; // 高32位: 时间戳, 低32位: 令牌数

        TokenBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            long now = System.currentTimeMillis();
            this.tokensAndTime = new AtomicLong(encode(now, capacity));
        }

        boolean tryAcquire() {
            int maxRetries = 64; // 防止极端情况下无限自旋
            for (int i = 0; i < maxRetries; i++) {
                long current = tokensAndTime.get();
                long lastTime = extractTime(current);
                long tokens = extractTokens(current);
                long now = System.currentTimeMillis();

                // 计算需要补充的令牌
                long elapsed = now - lastTime;
                if (elapsed >= 1000) {
                    long tokensToAdd = (elapsed / 1000) * refillRate;
                    tokens = Math.min(capacity, tokens + tokensToAdd);
                    lastTime = now;
                }

                // 尝试获取令牌
                if (tokens > 0) {
                    long newValue = encode(lastTime, tokens - 1);
                    if (tokensAndTime.compareAndSet(current, newValue)) {
                        return true;
                    }
                    // CAS失败，重试
                    continue;
                }
                return false;
            }
            return false;
        }

        private long encode(long time, long tokens) {
            return ((time & 0xFFFFFFFFL) << 32) | (tokens & 0xFFFFFFFFL);
        }

        private long extractTime(long encoded) {
            return (encoded >>> 32) & 0xFFFFFFFFL;
        }

        private long extractTokens(long encoded) {
            return encoded & 0xFFFFFFFFL;
        }
    }

    public static class Factory implements FilterFactory {
        @Override public Filter createFilter(FilterDefinition definition) { return new RequestRateLimiterFilter(definition); }
        @Override public String getSupportedFilterName() { return TYPE; }
        @Override public void validateConfig(FilterDefinition definition) {}
    }
}
