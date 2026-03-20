package com.muxin.gateway.core.route;

import com.muxin.gateway.core.connect.ClientConnection;
import com.muxin.gateway.core.connect.ServerConnection;
import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import com.muxin.gateway.core.service.EndpointAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认HTTP请求上下文实现
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class DefaultRequestContext implements RequestContext {

    private static final AtomicLong REQUEST_COUNTER = new AtomicLong(0);

    private final String requestId;
    private final long startTime;
    private final HttpServerExchange exchange;

    private volatile ServerConnection serverConnection;
    private volatile ClientConnection clientConnection;
    private volatile Route matchedRoute;
    private volatile EndpointAddress selectedEndpoint;

    private final AtomicBoolean completed = new AtomicBoolean(false);
    private volatile Throwable error;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>(16);

    public DefaultRequestContext(HttpServerExchange exchange) {
        this.exchange = Objects.requireNonNull(exchange, "HttpServerExchange不能为空");
        this.startTime = System.currentTimeMillis();
        this.requestId = generateRequestId();
        log.debug("创建请求上下文: {}", requestId);
    }

    private String generateRequestId() {
        long timestamp = System.currentTimeMillis();
        long counter = REQUEST_COUNTER.incrementAndGet() % 10000;
        return String.format("%d-%04d", timestamp, counter);
    }

    @Override
    public String requestId() {
        return requestId;
    }

    @Override
    public HttpServerExchange exchange() {
        return exchange;
    }

    @Override
    public ServerConnection serverConnection() {
        return serverConnection;
    }

    @Override
    public void setServerConnection(ServerConnection connection) {
        this.serverConnection = connection;
    }

    @Override
    public ClientConnection clientConnection() {
        return clientConnection;
    }

    @Override
    public void setClientConnection(ClientConnection connection) {
        this.clientConnection = connection;
    }

    @Override
    public Route getMatchedRoute() {
        return matchedRoute;
    }

    @Override
    public void setMatchedRoute(Route route) {
        this.matchedRoute = route;
        log.debug("设置匹配路由: {} -> {}", requestId, route != null ? route.getId() : "null");
    }

    @Override
    public EndpointAddress getSelectedEndpoint() {
        return selectedEndpoint;
    }

    @Override
    public void setSelectedEndpoint(EndpointAddress endpoint) {
        this.selectedEndpoint = endpoint;
        log.debug("设置选中端点: {} -> {}", requestId, endpoint != null ? endpoint.toUri() : "null");
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void markComplete() {
        if (completed.compareAndSet(false, true)) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.debug("请求完成: {} (耗时: {}ms)", requestId, totalTime);
        }
    }

    @Override
    public boolean isCompleted() {
        return completed.get();
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public void setError(Throwable error) {
        this.error = error;
        if (error != null) {
            log.warn("请求发生错误: {} - {}", requestId, error.getMessage());
            if (!isCompleted()) {
                markComplete();
            }
        }
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return String.format(
                "DefaultRequestContext{requestId='%s', route='%s', endpoint='%s', duration=%dms}",
                requestId,
                matchedRoute != null ? matchedRoute.getId() : "null",
                selectedEndpoint != null ? selectedEndpoint.toUri() : "null",
                System.currentTimeMillis() - startTime
        );
    }
}
