package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.Connection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.message.http.HttpResponseMessage;
import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import com.muxin.gateway.core.plus.route.DefaultRoute;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.filter.FilterChain;
import com.muxin.gateway.core.plus.route.filter.FilterType;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.InstanceManager;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网关处理器 - HTTP简化版本
 * 同步执行准备工作，只在必要时进行一次线程切换
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
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
        log.info("[GatewayProcessor] 网关处理器创建完成");
    }

    public final void processRequest(RequestContext context) {
        validateContext(context);

        try {
            if (log.isDebugEnabled()) {
                log.debug("[GatewayProcessor] 开始处理请求: {}", context.requestId());
            }

            prepareRequest(context);

            invokeBackendService(context)
                    .whenComplete((result, error) -> handleCompletion(context, result, error));
        } catch (Exception e) {
            handleError(context, e);
            cleanupResources(context);
        }
    }

    private void prepareRequest(RequestContext context) {
        Route route = routeManager.matchRoute(context);
        if (route == null) {
            throw new ProcessingException("路由匹配失败", context.requestId());
        }
        if (log.isDebugEnabled()) {
            log.debug("[GatewayProcessor] 路由匹配成功: {} -> {}", context.requestId(), route.getId());
        }

        executePreFilters(context, route);
        if (log.isDebugEnabled()) {
            log.debug("[GatewayProcessor] 前置过滤器执行完成: {}", context.requestId());
        }

        LoadBalanceStrategy strategy = route.getLoadBalanceStrategy();
        EndpointAddress endpoint = route.getService().selectTarget(context, strategy);
        if (endpoint == null) {
            throw new ProcessingException("端点选择失败", context.requestId());
        }
        context.setSelectedEndpoint(endpoint);
        if (log.isDebugEnabled()) {
            log.debug("[GatewayProcessor] 端点选择成功: {} -> {} (策略: {})", context.requestId(), endpoint.toUri(), strategy.getStrategyName());
        }

        ClientConnection connection = connectionPoolManager.getClientConnection(endpoint);
        if (connection == null) {
            throw new ProcessingException("连接获取失败", context.requestId());
        }
        context.setClientConnection(connection);
        if (log.isDebugEnabled()) {
            log.debug("[GatewayProcessor] 连接获取成功: {}", context.requestId());
        }
    }

    private void executePreFilters(RequestContext context, Route route) {
        context.exchange().setAttribute("matchedRoute", route);
        List<Filter> preFilters;
        if (route instanceof DefaultRoute dr) {
            preFilters = dr.getPreFilters();
        } else {
            preFilters = route.getFilters().stream()
                    .filter(f -> f.getType() == FilterType.PRE && f.isEnabled())
                    .sorted(Comparator.comparingInt(Filter::getOrder))
                    .toList();
        }
        FilterChain chain = FilterChain.create(preFilters);
        chain.doFilter(context.exchange());
    }

    private CompletableFuture<Void> invokeBackendService(RequestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("[GatewayProcessor] 开始后端调用: {}", context.requestId());
        }

        FullHttpRequest originalRequest = context.exchange().nettyRequest();
        FullHttpRequest requestToSend = buildBackendRequest(context, originalRequest);

        return context.clientConnection()
                .send(requestToSend)
                .thenAccept(response -> {
                    if (log.isDebugEnabled()) {
                        log.debug("[GatewayProcessor] 后端调用成功: {}", context.requestId());
                    }
                    setResponseToExchange(context, response);
                    executePostFilters(context);
                    sendResponse(context);
                    if (log.isDebugEnabled()) {
                        log.debug("[GatewayProcessor] 响应处理完成: {}", context.requestId());
                    }
                });
    }

    private FullHttpRequest buildBackendRequest(RequestContext context, FullHttpRequest request) {
        String strippedPath = (String) context.exchange().getAttribute("strippedPath");
        if (strippedPath != null && !strippedPath.equals(request.uri())) {
            if (log.isDebugEnabled()) {
                log.debug("[GatewayProcessor] 构建后端请求: {} -> {}", request.uri(), strippedPath);
            }
            request.setUri(strippedPath);
        }
        return request;
    }

    private void handleCompletion(RequestContext context, Void result, Throwable error) {
        try {
            if (error != null) {
                handleError(context, error);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("[GatewayProcessor] 请求处理成功: {}", context.requestId());
                }
            }
        } finally {
            cleanupResources(context);
        }
    }

    private void validateContext(RequestContext context) {
        Objects.requireNonNull(context, "RequestContext不能为空");
        Objects.requireNonNull(context.exchange(), "HttpServerExchange不能为空");
        Objects.requireNonNull(context.exchange().nettyRequest(), "请求消息不能为空");
    }

    private void executePostFilters(RequestContext context) {
        Route route = context.getMatchedRoute();
        List<Filter> postFilters;
        if (route instanceof DefaultRoute dr) {
            postFilters = dr.getPostFilters();
        } else {
            postFilters = route.getFilters().stream()
                    .filter(f -> f.getType() == FilterType.POST && f.isEnabled())
                    .sorted(Comparator.comparingInt(Filter::getOrder))
                    .toList();
        }
        FilterChain chain = FilterChain.create(postFilters);
        chain.doFilter(context.exchange());
    }

    private void setResponseToExchange(RequestContext context, Object response) {
        try {
            if (response == null) {
                log.warn("[GatewayProcessor] 后端响应为空，请求ID: {}", context.requestId());
                throw new ProcessingException("后端响应为空", context.requestId());
            }

            HttpServerExchange exchange = context.exchange();
            if (response instanceof HttpResponseMessage) {
                exchange.setResponse((HttpResponseMessage) response);
            } else if (response instanceof FullHttpResponse) {
                exchange.setNettyResponse((FullHttpResponse) response);
            } else {
                throw new ProcessingException("未知的响应类型: " + response.getClass(), context.requestId());
            }

            if (log.isDebugEnabled()) {
                log.debug("[GatewayProcessor] 响应设置到Exchange成功，请求ID: {}", context.requestId());
            }
        } catch (Exception e) {
            log.error("[GatewayProcessor] 设置响应失败，请求ID: {}", context.requestId(), e);
            throw new ProcessingException("设置响应失败", context.requestId(), e);
        }
    }

    private void sendResponse(RequestContext context) {
        Optional.ofNullable(context.serverConnection())
                .ifPresent(conn -> conn.sendResponse(context.exchange().nettyResponse())
                        .exceptionally(error -> {
                            log.error("[GatewayProcessor] 响应发送失败: {}", context.requestId(), error);
                            return null;
                        }));
    }

    private void handleError(RequestContext context, Throwable error) {
        log.error("[GatewayProcessor] 请求处理失败: {}", context.requestId(), error);
        context.setError(error);
        Optional.ofNullable(context.serverConnection())
                .ifPresent(conn -> conn.sendError(error)
                        .exceptionally(sendError -> {
                            log.error("[GatewayProcessor] 错误响应发送失败: {}", context.requestId(), sendError);
                            return null;
                        }));
    }

    private void cleanupResources(RequestContext context) {
        try {
            Optional.ofNullable(context.clientConnection())
                    .filter(Connection::isActive)
                    .ifPresent(ClientConnection::returnToPool);
            context.markComplete();
            if (log.isDebugEnabled()) {
                log.debug("[GatewayProcessor] 资源清理完成: {}", context.requestId());
            }
        } catch (Exception e) {
            log.warn("[GatewayProcessor] 资源清理异常: {}", context.requestId(), e);
        }
    }

    @Override
    public void init() {
        log.info("[GatewayProcessor] 初始化网关处理器组件");
        connectionPoolManager.init();
        routeManager.init();
        instanceManager.init();
        log.info("[GatewayProcessor] 网关处理器组件初始化完成");
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        init();
        connectionPoolManager.start();
        routeManager.start();
        instanceManager.start();
        running = true;
        log.info("[GatewayProcessor] 网关处理器启动完成");
    }

    @Override
    public void shutdown() {
        if (!running) {
            return;
        }
        running = false;
        log.info("[GatewayProcessor] 开始关闭网关处理器");
        businessExecutor.shutdown();
        instanceManager.shutdown();
        routeManager.shutdown();
        connectionPoolManager.shutdown();
        log.info("[GatewayProcessor] 网关处理器关闭完成");
    }

    public static class ProcessingException extends RuntimeException {
        private final String requestId;

        public ProcessingException(String message, String requestId) {
            super(String.format("[%s] %s", requestId, message));
            this.requestId = requestId;
        }

        public ProcessingException(String message, String requestId, Throwable cause) {
            super(String.format("[%s] %s", requestId, message), cause);
            this.requestId = requestId;
        }

        public String getRequestId() {
            return requestId;
        }
    }
}
