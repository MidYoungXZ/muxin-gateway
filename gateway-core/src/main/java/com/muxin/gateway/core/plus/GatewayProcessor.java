package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.Connection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.message.http.HttpResponseMessage;
import com.muxin.gateway.core.plus.message.http.HttpServerExchange;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.filter.FilterChain;
import com.muxin.gateway.core.plus.route.filter.FilterType;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.InstanceManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
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
            log.debug("[GatewayProcessor] 开始处理请求: {}", context.requestId());

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
        context.setMatchedRoute(route);
        log.debug("[GatewayProcessor] 路由匹配成功: {} -> {}", context.requestId(), route.getId());

        int stripPrefixCount = 0;
        for (com.muxin.gateway.core.plus.route.predicate.Predicate predicate : route.getPredicates()) {
            if (predicate instanceof com.muxin.gateway.core.plus.route.predicate.PathPredicate) {
                com.muxin.gateway.core.plus.route.predicate.PathPredicate pathPredicate = 
                    (com.muxin.gateway.core.plus.route.predicate.PathPredicate) predicate;
                if (pathPredicate.getStripPrefixCount() > 0) {
                    stripPrefixCount = pathPredicate.getStripPrefixCount();
                    context.exchange().setAttribute("stripPrefixCount", stripPrefixCount);
                    String originalPath = context.exchange().request().fullPath();
                    String strippedPath = pathPredicate.stripPrefix(originalPath);
                    context.exchange().setAttribute("strippedPath", strippedPath);
                    log.info("[GatewayProcessor] 路径前缀剥离: {} -> {} (剥离{}段)", 
                        originalPath, strippedPath, stripPrefixCount);
                }
            }
        }

        executeFilters(context, FilterType.PRE);
        log.debug("[GatewayProcessor] 前置过滤器执行完成: {}", context.requestId());

        LoadBalanceStrategy strategy = route.getLoadBalanceStrategy();
        EndpointAddress endpoint = route.getService().selectTarget(context, strategy);
        if (endpoint == null) {
            throw new ProcessingException("端点选择失败", context.requestId());
        }
        context.setSelectedEndpoint(endpoint);
        log.debug("[GatewayProcessor] 端点选择成功: {} -> {} (策略: {})",
                context.requestId(), endpoint.toUri(), strategy.getStrategyName());

        ClientConnection connection = connectionPoolManager.getClientConnection(endpoint);
        if (connection == null) {
            throw new ProcessingException("连接获取失败", context.requestId());
        }
        context.setClientConnection(connection);
        log.debug("[GatewayProcessor] 连接获取成功: {}", context.requestId());
    }

    private CompletableFuture<Void> invokeBackendService(RequestContext context) {
        log.debug("[GatewayProcessor] 开始后端调用: {}", context.requestId());

        FullHttpRequest originalRequest = context.exchange().nettyRequest();
        FullHttpRequest requestToSend = buildBackendRequest(context, originalRequest);

        return context.clientConnection()
                .send(requestToSend)
                .thenAccept(response -> {
                    log.debug("[GatewayProcessor] 后端调用成功: {}", context.requestId());
                    setResponseToExchange(context, response);
                    executeFilters(context, FilterType.POST);
                    sendResponse(context);
                    log.debug("[GatewayProcessor] 响应处理完成: {}", context.requestId());
                });
    }

    private FullHttpRequest buildBackendRequest(RequestContext context, FullHttpRequest original) {
        String strippedPath = (String) context.exchange().getAttribute("strippedPath");
        if (strippedPath != null && !strippedPath.equals(original.uri())) {
            log.info("[GatewayProcessor] 构建后端请求: {} -> {}", original.uri(), strippedPath);
            ByteBuf content = original.content();
            ByteBuf copiedContent = content != null && content.isReadable()
                    ? Unpooled.copiedBuffer(content)
                    : Unpooled.buffer(0);

            FullHttpRequest modified = new io.netty.handler.codec.http.DefaultFullHttpRequest(
                    original.protocolVersion(),
                    original.method(),
                    strippedPath,
                    copiedContent,
                    original.headers().copy(),
                    original.trailingHeaders().copy()
            );
            modified.headers().set(io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH, copiedContent.readableBytes());
            return modified;
        }
        return original;
    }

    private void handleCompletion(RequestContext context, Void result, Throwable error) {
        try {
            if (error != null) {
                handleError(context, error);
            } else {
                log.debug("[GatewayProcessor] 请求处理成功: {}", context.requestId());
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

    private void executeFilters(RequestContext context, FilterType type) {
        context.getMatchedRoute().getFilters().stream()
                .filter(f -> f.getType() == type && f.isEnabled())
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .forEach(filter -> {
                    try {
                        filter.filter(context.exchange(), NoOpFilterChain.INSTANCE);
                    } catch (Exception e) {
                        log.error("[GatewayProcessor] 过滤器执行失败: {} - {}", filter.getName(), context.requestId(), e);
                        throw new ProcessingException("过滤器执行失败: " + filter.getName(), context.requestId(), e);
                    }
                });
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
            } else if (response instanceof io.netty.handler.codec.http.FullHttpResponse) {
                exchange.setNettyResponse((io.netty.handler.codec.http.FullHttpResponse) response);
            } else {
                throw new ProcessingException("未知的响应类型: " + response.getClass(), context.requestId());
            }

            log.debug("[GatewayProcessor] 响应设置到Exchange成功，请求ID: {}", context.requestId());
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
            log.debug("[GatewayProcessor] 资源清理完成: {}", context.requestId());
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

    private static class NoOpFilterChain implements FilterChain {
        static final NoOpFilterChain INSTANCE = new NoOpFilterChain();

        @Override
        public void filter(HttpServerExchange exchange, FilterChain chain) {
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public void addFilter(Filter filter) {
        }

        @Override
        public int getCurrentIndex() {
            return 0;
        }

        @Override
        public int getTotalCount() {
            return 0;
        }

        @Override
        public void doFilter(HttpServerExchange exchange) {
        }
    }
}
