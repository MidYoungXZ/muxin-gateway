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
import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.service.EndpointAddress;
import com.muxin.gateway.core.service.ServiceRegistry;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class GatewayProcessor implements LifeCycle {

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

    /**
     * 请求处理主流程：
     * 路由匹配 → PRE过滤器 → 负载均衡选端点 → 发送到后端 → POST过滤器 → 响应客户端
     * <p>
     * 线程模型：无显式线程切换
     * - 同步阶段（路由匹配/过滤器/连接获取）在调用方线程执行
     * - 异步阶段（thenAccept/whenComplete）在后端响应回调线程执行
     * - 超时由 CompletableFuture.orTimeout 内置调度器驱动
     */
    public final void processRequest(RequestContext ctx) {
        Objects.requireNonNull(ctx, "RequestContext不能为空");
        Objects.requireNonNull(ctx.exchange(), "HttpServerExchange不能为空");

        try {
            // 1. 路由匹配
            Route route = routeManager.matchRoute(ctx);
            if (route == null) throw error("路由匹配失败", ctx.requestId());

            // 2. 执行 PRE 过滤器链
            FilterChain.create(route.getPreFilters()).doFilter(ctx.exchange());

            // 3. 选择后端端点并发送请求（含同步重试）
            long requestTimeout = route.getTimeout(TimeoutType.REQUEST);
            LoadBalanceStrategy lb = route.getLoadBalanceStrategy();
            int maxRetries = config.getCoreConfig() != null ? config.getCoreConfig().getMaxRetries() : 0;

            CompletableFuture<FullHttpResponse> backendFuture = selectAndSend(ctx, route, lb, maxRetries);

            // 4. 异步处理响应：超时控制 → POST过滤器 → 响应客户端 → 资源清理
            backendFuture.orTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                    .thenAccept(response -> {
                        if (ctx.isCompleted()) return;
                        ctx.exchange()._setNettyResponse(response);
                        FilterChain.create(route.getPostFilters()).doFilter(ctx.exchange());
                        sendResponse(ctx);
                    })
                    .whenComplete((v, ex) -> {
                        if (ex != null && !ctx.isCompleted()) {
                            handleError(ctx, unwrap(ex));
                        }
                        cleanup(ctx);
                    });

        } catch (Exception e) {
            handleError(ctx, e);
            cleanup(ctx);
        }
    }

    /**
     * 选择后端端点并通过连接池发送请求。
     * conn.send() 是非阻塞的（瞬间返回 CompletableFuture），因此重试在同步循环中完成。
     */
    private CompletableFuture<FullHttpResponse> selectAndSend(
            RequestContext ctx,
            Route route,
            LoadBalanceStrategy lb,
            int maxRetries) {

        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            ClientConnection conn = null;
            try {
                EndpointAddress endpoint = route.getService().selectTarget(ctx, lb);
                if (endpoint == null) throw error("端点选择失败", ctx.requestId());
                ctx.setSelectedEndpoint(endpoint);

                conn = connectionPoolManager.getClientConnection(endpoint);
                if (conn == null) throw error("连接获取失败", ctx.requestId());

                final ClientConnection finalConn = conn;
                ctx.setClientConnection(conn);

                return conn.send(ctx.exchange()._nettyRequest())
                        .whenComplete((response, ex) -> {
                            if (ex != null) {
                                ctx.setClientConnection(null);
                                safeReturnConnection(finalConn);
                            }
                        });
            } catch (Exception e) {
                lastException = e;
                safeReturnConnection(conn);
                if (attempt < maxRetries) {
                    log.warn("[GatewayProcessor] 重试 {}: {}", attempt + 1, ctx.requestId());
                }
            }
        }

        if (lastException instanceof RuntimeException re) throw re;
        throw lastException != null
                ? new RuntimeException(lastException)
                : error("发送失败", ctx.requestId());
    }

    private void safeReturnConnection(ClientConnection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.returnToPool();
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 归还连接失败: {}", e.getMessage());
        }
    }

    private Throwable unwrap(Throwable ex) {
        return ex instanceof CompletionException && ex.getCause() != null ? ex.getCause() : ex;
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
        serviceRegistry.shutdown();
        routeManager.shutdown();
        connectionPoolManager.shutdown();
        log.info("[GatewayProcessor] 网关处理器关闭完成");
    }
}
