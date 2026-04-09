package com.muxin.gateway.core;

import com.muxin.gateway.core.common.LifeCycle;
import com.muxin.gateway.core.config.GatewayConfig;
import com.muxin.gateway.core.connect.ClientConnection;
import com.muxin.gateway.core.connect.Connection;
import com.muxin.gateway.core.connect.ConnectionPoolManager;
import com.muxin.gateway.core.route.RequestContext;
import com.muxin.gateway.core.route.Route;
import com.muxin.gateway.core.route.RouteManager;
import com.muxin.gateway.core.route.TimeoutType;
import com.muxin.gateway.core.route.filter.FilterChain;
import com.muxin.gateway.core.route.loadbalance.LeastConnectionsLoadBalanceStrategy;
import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.service.EndpointAddress;
import com.muxin.gateway.core.service.ServiceRegistry;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class GatewayProcessor implements LifeCycle {

    private static final ScheduledExecutorService TIMEOUT_SCHEDULER = Executors.newScheduledThreadPool(
            2, r -> {
                Thread t = new Thread(r, "gateway-timeout");
                t.setDaemon(true);
                return t;
            }
    );

    protected final GatewayConfig config;
    protected final ConnectionPoolManager connectionPoolManager;
    protected final RouteManager routeManager;
    protected final ServiceRegistry serviceRegistry;
    protected volatile boolean running = false;

    public GatewayProcessor(GatewayConfig config,
                            ConnectionPoolManager connectionPoolManager,
                            RouteManager routeManager,
                            ServiceRegistry serviceRegistry) {
        this.config = config;
        this.connectionPoolManager = connectionPoolManager;
        this.routeManager = routeManager;
        this.serviceRegistry = serviceRegistry;
    }

    public final void processRequest(RequestContext ctx) {
        Objects.requireNonNull(ctx, "RequestContext不能为空");
        Objects.requireNonNull(ctx.exchange(), "HttpServerExchange不能为空");

        try {
            Route route = routeManager.matchRoute(ctx);
            if (route == null) throw error("路由匹配失败", ctx.requestId());
            ctx.setMatchedRoute(route);

            FilterChain.create(route.getPreFilters()).doFilter(ctx.exchange());

            long requestTimeout = route.getTimeout(TimeoutType.REQUEST);
            long totalTimeout = route.getTimeout(TimeoutType.TOTAL);

            ScheduledFuture<?> globalTimeout = TIMEOUT_SCHEDULER.schedule(
                    () -> forceTimeout(ctx), totalTimeout, TimeUnit.MILLISECONDS);

            LoadBalanceStrategy lb = route.getLoadBalanceStrategy();
            int maxRetries = config.getCoreConfig() != null ? config.getCoreConfig().getMaxRetries() : 0;

            CompletableFuture<FullHttpResponse> backendFuture =
                    selectAndSend(ctx, route, lb, maxRetries);

            backendFuture
                    .orTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                    .thenAccept(response -> {
                        if (ctx.isCompleted()) return;
                        ctx.exchange()._setNettyResponse(response);
                        FilterChain.create(route.getPostFilters()).doFilter(ctx.exchange());
                        sendResponse(ctx);
                    })
                    .whenComplete((v, ex) -> {
                        globalTimeout.cancel(false);
                        if (ex != null && !ctx.isCompleted()) handleError(ctx, unwrap(ex));
                        decrementLb(lb, ctx.getSelectedEndpoint());
                        cleanup(ctx);
                    });

        } catch (Exception e) {
            handleError(ctx, e);
            cleanup(ctx);
        }
    }

    private CompletableFuture<FullHttpResponse> selectAndSend(
            RequestContext ctx, Route route, LoadBalanceStrategy lb, int maxRetries) {

        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                EndpointAddress endpoint = route.getService().selectTarget(ctx, lb);
                if (endpoint == null) throw error("端点选择失败", ctx.requestId());
                ctx.setSelectedEndpoint(endpoint);

                ClientConnection conn = connectionPoolManager.getClientConnection(endpoint);
                if (conn == null) throw error("连接获取失败", ctx.requestId());
                ctx.setClientConnection(conn);

                String strippedPath = ctx.exchange().getAttribute("strippedPath");
                if (strippedPath != null) ctx.exchange().uri(strippedPath);

                return conn.send(ctx.exchange()._nettyRequest());
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    log.warn("[GatewayProcessor] 重试 {}: {}", attempt + 1, ctx.requestId());
                    decrementLb(lb, ctx.getSelectedEndpoint());
                }
            }
        }

        if (lastException instanceof RuntimeException re) throw re;
        throw lastException != null
                ? new RuntimeException(lastException)
                : error("发送失败", ctx.requestId());
    }

    private void forceTimeout(RequestContext ctx) {
        if (ctx.isCompleted()) return;
        log.warn("[GatewayProcessor] 全局超时: {}", ctx.requestId());
        ctx.exchange().setStatus(HttpResponseStatus.GATEWAY_TIMEOUT);
        ctx.exchange().setResponseHeader("Content-Type", "application/json");
        ctx.exchange().setResponseBody("{\"code\":504,\"message\":\"Gateway Timeout\"}");
        sendResponse(ctx);
        ctx.markComplete();
    }

    private Throwable unwrap(Throwable ex) {
        return ex instanceof CompletionException && ex.getCause() != null ? ex.getCause() : ex;
    }

    private void decrementLb(LoadBalanceStrategy lb, EndpointAddress endpoint) {
        if (endpoint != null && lb instanceof LeastConnectionsLoadBalanceStrategy lc) {
            lc.decrementConnectionCount(endpoint);
        }
    }

    private void sendResponse(RequestContext ctx) {
        Optional.ofNullable(ctx.serverConnection())
                .ifPresent(conn -> conn.sendResponse(ctx.exchange()._nettyResponse())
                        .exceptionally(e -> {
                            log.error("[GatewayProcessor] 响应发送失败: {}", ctx.requestId(), e);
                            return null;
                        }));
    }

    private void handleError(RequestContext ctx, Throwable error) {
        log.error("[GatewayProcessor] 请求处理失败: {}", ctx.requestId(), error);
        ctx.setError(error);
        Optional.ofNullable(ctx.serverConnection())
                .ifPresent(conn -> conn.sendError(error)
                        .exceptionally(e -> {
                            log.error("[GatewayProcessor] 错误响应发送失败: {}", ctx.requestId(), e);
                            return null;
                        }));
    }

    private void cleanup(RequestContext ctx) {
        try {
            Optional.ofNullable(ctx.clientConnection())
                    .filter(Connection::isActive)
                    .ifPresent(ClientConnection::returnToPool);
            ctx.markComplete();
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 资源清理异常: {}", ctx.requestId(), e);
        }
    }

    private RuntimeException error(String msg, String requestId) {
        return new RuntimeException("[" + requestId + "] " + msg);
    }

    @Override
    public void init() {
        connectionPoolManager.init();
        routeManager.init();
        serviceRegistry.init();
    }

    @Override
    public void start() {
        if (running) return;
        init();
        connectionPoolManager.start();
        routeManager.start();
        serviceRegistry.start();
        running = true;
        log.info("[GatewayProcessor] 网关处理器启动完成");
    }

    @Override
    public void shutdown() {
        if (!running) return;
        running = false;
        TIMEOUT_SCHEDULER.shutdown();
        serviceRegistry.shutdown();
        routeManager.shutdown();
        connectionPoolManager.shutdown();
        log.info("[GatewayProcessor] 网关处理器关闭完成");
    }
}
