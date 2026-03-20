package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.Connection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.route.DefaultRoute;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.filter.FilterChain;
import com.muxin.gateway.core.plus.route.filter.FilterType;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.InstanceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class GatewayProcessor implements LifeCycle {

    protected final GatewayConfig config;
    protected final ConnectionPoolManager connectionPoolManager;
    protected final RouteManager routeManager;
    protected final InstanceManager instanceManager;
    protected final ExecutorService businessExecutor;
    protected volatile boolean running = false;

    public GatewayProcessor(GatewayConfig config,
                            ConnectionPoolManager connectionPoolManager,
                            RouteManager routeManager,
                            InstanceManager instanceManager) {
        this.config = config;
        this.connectionPoolManager = connectionPoolManager;
        this.routeManager = routeManager;
        this.instanceManager = instanceManager;
        this.businessExecutor = Executors.newFixedThreadPool(16, r -> {
            Thread thread = new Thread(r, "gateway-business-" + System.nanoTime());
            thread.setDaemon(false);
            return thread;
        });
    }

    public final void processRequest(RequestContext ctx) {
        Objects.requireNonNull(ctx, "RequestContext不能为空");
        Objects.requireNonNull(ctx.exchange(), "HttpServerExchange不能为空");

        try {
            Route route = routeManager.matchRoute(ctx);
            if (route == null) {
                throw error("路由匹配失败", ctx.requestId());
            }

            executeFilters(ctx, getFilters(route, FilterType.PRE));

            EndpointAddress endpoint = route.getService().selectTarget(ctx, route.getLoadBalanceStrategy());
            if (endpoint == null) throw error("端点选择失败", ctx.requestId());
            ctx.setSelectedEndpoint(endpoint);

            ClientConnection conn = connectionPoolManager.getClientConnection(endpoint);
            if (conn == null) throw error("连接获取失败", ctx.requestId());
            ctx.setClientConnection(conn);

            String strippedPath = ctx.exchange().getAttribute("strippedPath");
            if (strippedPath != null) {
                ctx.exchange().uri(strippedPath);
            }

            conn.send(ctx.exchange()._nettyRequest())
                    .thenAccept(response -> {
                        ctx.exchange()._setNettyResponse(response);
                        executeFilters(ctx, getFilters(route, FilterType.POST));
                        sendResponse(ctx);
                    })
                    .whenComplete((v, e) -> {
                        if (e != null) handleError(ctx, e);
                        cleanup(ctx);
                    });

        } catch (Exception e) {
            handleError(ctx, e);
            cleanup(ctx);
        }
    }

    private List<Filter> getFilters(Route route, FilterType type) {
        if (route instanceof DefaultRoute dr) {
            return type == FilterType.PRE ? dr.getPreFilters() : dr.getPostFilters();
        }
        return route.getFilters().stream()
                .filter(f -> f.getType() == type && f.isEnabled())
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .toList();
    }

    private void executeFilters(RequestContext ctx, List<Filter> filters) {
        ctx.exchange().setAttribute("matchedRoute", ctx.getMatchedRoute());
        FilterChain.create(filters).doFilter(ctx.exchange());
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
        instanceManager.init();
    }

    @Override
    public void start() {
        if (running) return;
        init();
        connectionPoolManager.start();
        routeManager.start();
        instanceManager.start();
        running = true;
        log.info("[GatewayProcessor] 网关处理器启动完成");
    }

    @Override
    public void shutdown() {
        if (!running) return;
        running = false;
        businessExecutor.shutdown();
        instanceManager.shutdown();
        routeManager.shutdown();
        connectionPoolManager.shutdown();
        log.info("[GatewayProcessor] 网关处理器关闭完成");
    }
}