package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.Connection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.ServerExchange;
import com.muxin.gateway.core.plus.route.RequestContext;
import com.muxin.gateway.core.plus.route.Route;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.filter.FilterChain;
import com.muxin.gateway.core.plus.route.filter.FilterType;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import com.muxin.gateway.core.plus.route.service.InstanceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * 网关处理器 - 优雅版本
 * 同步执行准备工作，只在必要时进行一次线程切换
 *
 * @author muxin
 */
@Slf4j
public class GatewayProcessor implements LifeCycle {

    // ========== 核心组件依赖 ==========
    protected final GatewayConfig config;
    protected final ConnectionPoolManager connectionPoolManager;
    protected final RouteManager routeManager;
    protected final InstanceManager instanceManager;

    // ========== 线程池管理 ==========
    protected final ExecutorService businessExecutor;

    // ========== 状态管理 ==========
    protected volatile boolean running = false;

    public GatewayProcessor(GatewayConfig config,
                            ConnectionPoolManager connectionPoolManager,
                            RouteManager routeManager,
                            InstanceManager instanceManager) {
        this.config = config;
        this.connectionPoolManager = connectionPoolManager;
        this.routeManager = routeManager;
        this.instanceManager = instanceManager;

        // 初始化业务线程池
        this.businessExecutor = Executors.newFixedThreadPool(
                16, // 默认业务线程池大小
                r -> {
                    Thread thread = new Thread(r, "gateway-business-" + System.nanoTime());
                    thread.setDaemon(false);
                    return thread;
                }
        );

        log.info("[GatewayProcessor] 网关处理器创建完成");
    }

    /**
     * 处理入站请求 - 真正优雅的版本
     * 同步执行准备工作，只在必要时进行一次线程切换
     */
    public final void processRequest(RequestContext context) {
        validateContext(context);

        try {
            log.debug("[GatewayProcessor] 开始处理请求: {}", context.requestId());

            // 同步阶段：直接执行，无线程切换
            prepareRequest(context);

            // 异步阶段：唯一的线程切换点
            invokeBackendService(context)
                    .whenComplete((result, error)
                            -> handleCompletion(context, result, error)
                    );
        } catch (Exception e) {
            handleError(context, e);
            cleanupResources(context);
        }
    }

    /**
     * 同步准备请求 - 直接调用，无额外开销
     */
    private void prepareRequest(RequestContext context) {
        // 路由匹配
        Route route = routeManager.matchRoute(context);
        requireNonNull(route, () -> new ProcessingException("路由匹配失败", context.requestId()));
        context.setMatchedRoute(route);
        log.debug("[GatewayProcessor] 路由匹配成功: {} -> {}", context.requestId(), route.getId());

        // 前置过滤器
        executeFilters(context, FilterType.PRE);
        log.debug("[GatewayProcessor] 前置过滤器执行完成: {}", context.requestId());

        // 端点选择
        LoadBalanceStrategy strategy = route.getLoadBalanceStrategy();
        EndpointAddress endpoint = route.getService().selectTarget(context, strategy);
        requireNonNull(endpoint, () -> new ProcessingException("端点选择失败", context.requestId()));
        context.setSelectedEndpoint(endpoint);
        log.debug("[GatewayProcessor] 端点选择成功: {} -> {} (策略: {})", 
                context.requestId(), endpoint.toUri(), strategy.getStrategyName());

        // 连接获取
        ClientConnection connection = connectionPoolManager.getClientConnection(endpoint, endpoint.getProtocol());
        requireNonNull(connection, () -> new ProcessingException("连接获取失败", context.requestId()));
        context.setClientConnection(connection);
        log.debug("[GatewayProcessor] 连接获取成功: {}", context.requestId());
    }

    /**
     * 后端服务调用 - 唯一的异步操作
     */
    private CompletableFuture<Void> invokeBackendService(RequestContext context) {
        log.debug("[GatewayProcessor] 开始后端调用: {}", context.requestId());

        return context.clientConnection()
                .send(context.exchange().request())
                .thenAccept(response -> {
                    // 在I/O线程中执行后续处理
                    log.debug("[GatewayProcessor] 后端调用成功: {}", context.requestId());
                    setResponseToExchange(context, response);
                    executeFilters(context, FilterType.POST);
                    sendResponse(context);
                    log.debug("[GatewayProcessor] 响应处理完成: {}", context.requestId());
                });
    }

    /**
     * 统一完成处理
     */
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

    /**
     * 验证上下文
     */
    private void validateContext(RequestContext context) {
        Objects.requireNonNull(context, "RequestContext不能为空");
        Objects.requireNonNull(context.exchange(), "ServerExchange不能为空");
        Objects.requireNonNull(context.exchange().request(), "请求消息不能为空");
    }

    /**
     * 需要时抛异常的工具方法
     */
    private static <T> T requireNonNull(T obj, Supplier<RuntimeException> exceptionSupplier) {
        if (obj == null) {
            throw exceptionSupplier.get();
        }
        return obj;
    }

    /**
     * 统一过滤器执行
     */
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

    /**
     * 设置响应到Exchange
     */
    private void setResponseToExchange(RequestContext context, Message response) {
        try {
            // 假设有可变的Exchange实现或通过其他方式设置响应
            // 这里可能需要根据具体的ServerExchange实现来调整

            // TODO: 根据实际的ServerExchange实现来设置响应
            // 目前先记录日志，具体实现可能需要强制转换或其他方式
            log.debug("[GatewayProcessor] 响应设置到Exchange: {}", context.requestId());

        } catch (Exception e) {
            log.error("[GatewayProcessor] 设置响应失败: {}", context.requestId(), e);
            throw new ProcessingException("设置响应失败", context.requestId(), e);
        }
    }

    /**
     * 发送响应
     */
    private void sendResponse(RequestContext context) {
        Optional.ofNullable(context.serverConnection())
                .ifPresent(conn -> conn.sendResponse(context.exchange().response())
                        .exceptionally(error -> {
                            log.error("[GatewayProcessor] 响应发送失败: {}", context.requestId(), error);
                            return null;
                        }));
    }

    /**
     * 优雅的错误处理
     */
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

    /**
     * 优雅的资源清理
     */
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

    // ========== 生命周期方法 ==========

    @Override
    public void init() {
        log.info("[GatewayProcessor] 初始化网关处理器组件");

        // 初始化各个组件
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
        // 启动各个组件
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

        // 关闭线程池
        businessExecutor.shutdown();

        instanceManager.shutdown();
        routeManager.shutdown();
        connectionPoolManager.shutdown();
        log.info("[GatewayProcessor] 网关处理器关闭完成");
    }

    // ========== 内部类 ==========

    /**
     * 处理异常 - 携带请求ID便于追踪
     */
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

    /**
     * 空操作过滤器链 - 简化实现
     */
    private static class NoOpFilterChain implements FilterChain {
        static final NoOpFilterChain INSTANCE = new NoOpFilterChain();

        @Override
        public void filter(ServerExchange<? extends Message, ? extends Message> exchange, FilterChain chain) {
            // 空实现 - 当前版本不需要链式调用
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public void addFilter(Filter filter) {
            // 空实现
        }

        @Override
        public int getCurrentIndex() {
            return 0;
        }

        @Override
        public int getTotalCount() {
            return 0;
        }
    }
} 