package com.muxin.gateway.core.plus.route.filter;

import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MetricsFilter extends AbstractFilter {

    public static final String TYPE = "METRICS";

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalResponses = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalRequestBytes = new AtomicLong(0);
    private final AtomicLong totalResponseBytes = new AtomicLong(0);

    public MetricsFilter() {
    }

    public MetricsFilter(FilterDefinition definition) {
        this.name = TYPE;
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    protected void doFilter(HttpServerExchange exchange, FilterChain chain) {
        totalRequests.incrementAndGet();

        try {
            chain.filter(exchange, chain);
            totalResponses.incrementAndGet();
        } catch (Exception e) {
            totalErrors.incrementAndGet();
            throw e;
        }
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getTotalResponses() {
        return totalResponses.get();
    }

    public long getTotalErrors() {
        return totalErrors.get();
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("totalResponses", totalResponses.get());
        metrics.put("totalErrors", totalErrors.get());
        metrics.put("successRate", totalRequests.get() > 0 
                ? (double) totalResponses.get() / totalRequests.get() 
                : 0.0);
        return metrics;
    }

    public static class Factory implements FilterFactory {

        @Override
        public Filter createFilter(FilterDefinition definition) {
            return new MetricsFilter(definition);
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