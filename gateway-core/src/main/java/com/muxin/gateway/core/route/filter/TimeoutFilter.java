package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class TimeoutFilter implements Filter {

    public static final String TYPE = "TimeoutFilter";

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r, "timeout-filter");
                t.setDaemon(true);
                return t;
            }
    );

    private final int connectTimeout;
    private final int responseTimeout;
    private final int order;
    private final boolean enabled;

    public TimeoutFilter(FilterDefinition definition) {
        this.connectTimeout = definition.getIntArg("connectTimeout", 5000);
        this.responseTimeout = definition.getIntArg("responseTimeout", 30000);
        this.order = definition.getOrder();
        this.enabled = definition.isEnabled();
    }

    @Override
    public void filter(HttpServerExchange exchange, FilterChain chain) {
        if (!enabled) {
            chain.doFilter(exchange);
            return;
        }

        exchange.setAttribute("connectTimeout", connectTimeout);
        exchange.setAttribute("responseTimeout", responseTimeout);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                chain.doFilter(exchange);
            } catch (Exception e) {
                log.error("[TimeoutFilter] 请求执行异常: {}", e.getMessage());
                exchange.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                exchange.setResponseHeader("Content-Type", "application/json");
                exchange.setResponseBody("{\"code\":500,\"message\":\"Internal Server Error\"}");
            }
        });

        ScheduledFuture<?> timeoutFuture = SCHEDULER.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
                log.warn("[TimeoutFilter] 请求超时, 路径: {}", exchange.fullPath());
                exchange.setStatus(HttpResponseStatus.GATEWAY_TIMEOUT);
                exchange.setResponseHeader("Content-Type", "application/json");
                exchange.setResponseBody("{\"code\":504,\"message\":\"Gateway Timeout\"}");
            }
        }, responseTimeout, TimeUnit.MILLISECONDS);

        future.whenComplete((result, ex) -> timeoutFuture.cancel(false));

        try {
            future.get(responseTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("[TimeoutFilter] 等待响应超时: {}", exchange.fullPath());
        } catch (Exception e) {
            log.error("[TimeoutFilter] 等待响应异常: {}", e.getMessage());
        }
    }

    @Override public String getName() { return TYPE; }
    @Override public FilterType getType() { return FilterType.PRE; }
    @Override public int getOrder() { return order; }
    @Override public boolean isEnabled() { return enabled; }

    public static class Factory implements FilterFactory {
        @Override public Filter createFilter(FilterDefinition definition) { return new TimeoutFilter(definition); }
        @Override public String getSupportedFilterName() { return TYPE; }
        @Override public void validateConfig(FilterDefinition definition) {}
    }
}
