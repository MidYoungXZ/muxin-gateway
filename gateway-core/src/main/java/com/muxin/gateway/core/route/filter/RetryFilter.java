package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class RetryFilter implements Filter {

    public static final String TYPE = "Retry";

    private final int retries;
    private final Set<String> retryStatuses;
    private final Set<String> retryMethods;
    private final long firstBackoff;
    private final long maxBackoff;
    private final double backoffFactor;
    private final int order;
    private final boolean enabled;

    public RetryFilter(int retries) {
        this(retries, Set.of("BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"), 
             Set.of("GET"), 10, 1000, 2.0, 0, true);
    }

    public RetryFilter(int retries, Set<String> retryStatuses, Set<String> retryMethods,
                       long firstBackoff, long maxBackoff, double backoffFactor, 
                       int order, boolean enabled) {
        this.retries = retries;
        this.retryStatuses = retryStatuses != null ? retryStatuses : Set.of("BAD_GATEWAY");
        this.retryMethods = retryMethods != null ? retryMethods : Set.of("GET");
        this.firstBackoff = firstBackoff;
        this.maxBackoff = maxBackoff;
        this.backoffFactor = backoffFactor;
        this.order = order;
        this.enabled = enabled;
    }

    @SuppressWarnings("unchecked")
    public RetryFilter(FilterDefinition definition) {
        Map<String, Object> args = definition.getArgs();
        this.retries = args != null ? getIntValue(args.get("retries"), 3) : 3;
        this.retryStatuses = parseStringSet(args != null ? args.get("statuses") : null, 
                                             Set.of("BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"));
        this.retryMethods = parseStringSet(args != null ? args.get("methods") : null, 
                                            Set.of("GET"));
        
        Map<String, Object> backoff = args != null ? (Map<String, Object>) args.get("backoff") : null;
        this.firstBackoff = backoff != null ? parseBackoff(backoff.get("firstBackoff"), 10) : 10;
        this.maxBackoff = backoff != null ? parseBackoff(backoff.get("maxBackOff"), 1000) : 1000;
        this.backoffFactor = backoff != null ? getDoubleValue(backoff.get("factor"), 2.0) : 2.0;
        
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

    private double getDoubleValue(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long parseBackoff(Object value, long defaultValue) {
        if (value == null) return defaultValue;
        String str = value.toString();
        if (str.endsWith("ms")) {
            return Long.parseLong(str.replace("ms", ""));
        } else if (str.endsWith("s")) {
            return Long.parseLong(str.replace("s", "")) * 1000;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> parseStringSet(Object value, Set<String> defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof List) {
            Set<String> result = new HashSet<>();
            for (Object item : (List<?>) value) {
                if (item != null) {
                    result.add(item.toString().toUpperCase());
                }
            }
            return result;
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.contains(",")) {
                Set<String> result = new HashSet<>();
                for (String part : str.split(",")) {
                    result.add(part.trim().toUpperCase());
                }
                return result;
            }
            return Set.of(str.toUpperCase());
        }
        return defaultValue;
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled || retries <= 0) {
            chain.doFilter(exchange);
            return;
        }

        String method = exchange.method();
        if (!retryMethods.contains(method)) {
            chain.doFilter(exchange);
            return;
        }

        int attempt = 0;
        Exception lastException = null;
        HttpResponseStatus lastStatus = null;

        while (attempt <= retries) {
            try {
                if (attempt > 0) {
                    long backoff = calculateBackoff(attempt);
                    if (log.isDebugEnabled()) {
                        log.debug("[RetryFilter] 第 {} 次重试, 等待 {} ms", attempt, backoff);
                    }
                    Thread.sleep(backoff);
                }

                chain.doFilter(exchange);

                lastStatus = exchange.status();
                if (lastStatus != null && !shouldRetry(lastStatus)) {
                    return;
                }

                if (lastStatus == null || lastStatus.code() < 400) {
                    return;
                }

                attempt++;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("[RetryFilter] 请求失败: {}", e.getMessage());
            }
        }

        if (lastException != null) {
            log.error("[RetryFilter] 重试 {} 次后仍然失败", retries);
        } else if (lastStatus != null) {
            log.error("[RetryFilter] 重试 {} 次后仍然返回状态码: {}", retries, lastStatus.code());
        }
    }

    private boolean shouldRetry(HttpResponseStatus status) {
        return retryStatuses.contains(status.codeAsText().toString());
    }

    private long calculateBackoff(int attempt) {
        long backoff = (long) (firstBackoff * Math.pow(backoffFactor, attempt - 1));
        backoff = Math.min(backoff, maxBackoff);
        backoff = backoff + ThreadLocalRandom.current().nextLong(0, backoff / 10 + 1);
        return backoff;
    }

    @Override
    public String getName() {
        return "RetryFilter";
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
            return new RetryFilter(definition);
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