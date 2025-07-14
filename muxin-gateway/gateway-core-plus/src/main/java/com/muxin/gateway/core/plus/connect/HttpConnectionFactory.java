package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.protocol.message.ProtocolEnum;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP连接工厂实现
 * 基于Netty创建HTTP连接，专为gateway-core-plus设计
 * 
 * @author muxin
 */
@Slf4j
public class HttpConnectionFactory implements ConnectionFactory {

    private final HttpConnectionFactoryConfig config;
    private final HttpConnectionFactoryStats stats;
    private final EventLoopGroup workerGroup;
    private volatile boolean shutdown = false;

    public HttpConnectionFactory() {
        this.config = new DefaultHttpConnectionFactoryConfig();
        this.stats = new DefaultHttpConnectionFactoryStats();
        this.workerGroup = new NioEventLoopGroup(4); // 4个工作线程
        log.info("[HttpConnectionFactory] HTTP连接工厂初始化完成");
    }

    public HttpConnectionFactory(HttpConnectionFactoryConfig config) {
        this.config = config;
        this.stats = new DefaultHttpConnectionFactoryStats();
        this.workerGroup = new NioEventLoopGroup(4);
        log.info("[HttpConnectionFactory] HTTP连接工厂初始化完成，使用自定义配置: {}", config.getName());
    }

    @Override
    public Protocol getSupportedProtocol() {
        return ProtocolEnum.HTTP;
    }

    @Override
    public boolean supports(Protocol protocol) {
        return ProtocolEnum.HTTP.equals(protocol);
    }

    @Override
    public ServerConnection createServerConnection(Object protocolContext) throws ConnectionCreationException {
        try {
            if (!(protocolContext instanceof ChannelHandlerContext)) {
                throw new ConnectionCreationException(
                    "HTTP协议需要ChannelHandlerContext作为协议上下文", 
                    getSupportedProtocol()
                );
            }

            ChannelHandlerContext ctx = (ChannelHandlerContext) protocolContext;
            ServerConnection connection = new NettyServerConnection(ctx, getSupportedProtocol());
            
            stats.incrementConnectionsCreated();
            log.debug("[HttpConnectionFactory] 创建服务端连接: {}", connection.getConnectionId());
            return connection;
            
        } catch (Exception e) {
            stats.incrementConnectionsFailed();
            throw new ConnectionCreationException(
                "创建HTTP服务端连接失败: " + e.getMessage(), 
                e, 
                getSupportedProtocol(), 
                null
            );
        }
    }

    @Override
    public CompletableFuture<ClientConnection> createClientConnection(EndpointAddress target, Map<String, Object> options) {
        if (isShutdown()) {
            return CompletableFuture.failedFuture(
                new ConnectionCreationException("连接工厂已关闭", getSupportedProtocol(), target)
            );
        }

        CompletableFuture<ClientConnection> future = new CompletableFuture<>();
        long startTime = System.currentTimeMillis();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (Integer) options.getOrDefault("connectTimeout", 5000))
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, (Boolean) options.getOrDefault("keepAlive", true))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(1048576)); // 1MB
                        }
                    });

            String host = target.getHost();
            int port = target.getPort();

            ChannelFuture channelFuture = bootstrap.connect(host, port);
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isSuccess()) {
                    try {
                        Channel channel = channelFuture1.channel();
                        ClientConnection connection = new NettyClientConnection(target, getSupportedProtocol(), channel, options);
                        
                        long duration = System.currentTimeMillis() - startTime;
                        stats.recordConnectionTime(duration);
                        stats.incrementConnectionsCreated();
                        
                        log.debug("[HttpConnectionFactory] 创建客户端连接成功: {} -> {}, 耗时: {}ms", 
                            connection.getConnectionId(), target.toUri(), duration);
                        future.complete(connection);
                    } catch (Exception e) {
                        stats.incrementConnectionsFailed();
                        future.completeExceptionally(new ConnectionCreationException(
                            "创建HTTP客户端连接失败: " + e.getMessage(), 
                            e, 
                            getSupportedProtocol(), 
                            target
                        ));
                    }
                } else {
                    stats.incrementConnectionsFailed();
                    future.completeExceptionally(new ConnectionCreationException(
                        "连接到目标地址失败: " + channelFuture1.cause().getMessage(), 
                        channelFuture1.cause(), 
                        getSupportedProtocol(), 
                        target
                    ));
                }
            });

        } catch (Exception e) {
            stats.incrementConnectionsFailed();
            future.completeExceptionally(new ConnectionCreationException(
                "创建HTTP客户端连接失败: " + e.getMessage(), 
                e, 
                getSupportedProtocol(), 
                target
            ));
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> validateConnection(Connection connection) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (connection == null) {
                    return false;
                }
                
                // 基础活跃性检查
                if (!connection.isActive()) {
                    log.debug("[HttpConnectionFactory] 连接已断开: {}", connection.getConnectionId());
                    return false;
                }

                // 对于客户端连接，检查健康状态
                if (connection instanceof ClientConnection) {
                    ClientConnection clientConn = (ClientConnection) connection;
                    boolean healthy = clientConn.isHealthy();
                    log.debug("[HttpConnectionFactory] 客户端连接健康检查: {} -> {}", 
                        connection.getConnectionId(), healthy);
                    return healthy;
                }

                return true;
            } catch (Exception e) {
                log.debug("[HttpConnectionFactory] 连接验证异常: {}", connection.getConnectionId(), e);
                return false;
            }
        });
    }

    @Override
    public ConnectionHealthStatus getConnectionHealth(Connection connection) {
        try {
            if (connection == null) {
                return ConnectionHealthStatus.UNKNOWN;
            }

            if (!connection.isActive()) {
                return ConnectionHealthStatus.UNHEALTHY;
            }

            if (connection instanceof ClientConnection) {
                ClientConnection clientConn = (ClientConnection) connection;
                if (!clientConn.isHealthy()) {
                    return ConnectionHealthStatus.UNHEALTHY;
                }

                // 检查连接统计信息
                long totalRequests = clientConn.getTotalRequests();
                long totalFailures = clientConn.getTotalFailures();
                
                if (totalRequests > 0) {
                    double failureRate = (double) totalFailures / totalRequests;
                    if (failureRate > 0.1) { // 失败率超过10%为警告状态
                        return ConnectionHealthStatus.WARNING;
                    }
                }
            }

            return ConnectionHealthStatus.HEALTHY;
        } catch (Exception e) {
            log.debug("[HttpConnectionFactory] 获取连接健康状态异常: {}", 
                connection != null ? connection.getConnectionId() : "null", e);
            return ConnectionHealthStatus.UNKNOWN;
        }
    }

    @Override
    public Map<String, Object> getDefaultConnectionOptions() {
        Map<String, Object> options = new ConcurrentHashMap<>();
        options.put("connectTimeout", config.getConnectionTimeout());
        options.put("keepAlive", config.isKeepAliveEnabled());
        options.put("protocol", getSupportedProtocol());
        return options;
    }

    @Override
    public ConnectionFactoryConfig getConfig() {
        return config;
    }

    @Override
    public void updateConfig(ConnectionFactoryConfig config) {
        if (config instanceof HttpConnectionFactoryConfig) {
            // 这里可以实现配置更新逻辑
            log.info("[HttpConnectionFactory] 配置更新: {}", config.getName());
        } else {
            log.warn("[HttpConnectionFactory] 不支持的配置类型: {}", config.getClass().getName());
        }
    }

    @Override
    public ConnectionFactoryStats getStats() {
        return stats;
    }

    @Override
    public CompletableFuture<Void> warmup() {
        return CompletableFuture.runAsync(() -> {
            log.info("[HttpConnectionFactory] 开始预热连接工厂");
            try {
                // 预热操作：预热线程池等
                Thread.sleep(100); // 模拟预热时间
                log.info("[HttpConnectionFactory] 连接工厂预热完成");
            } catch (Exception e) {
                log.warn("[HttpConnectionFactory] 连接工厂预热异常", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            log.info("[HttpConnectionFactory] 开始关闭连接工厂");
            shutdown = true;
            try {
                if (workerGroup != null) {
                    workerGroup.shutdownGracefully().sync();
                }
                log.info("[HttpConnectionFactory] 连接工厂关闭完成");
            } catch (Exception e) {
                log.error("[HttpConnectionFactory] 关闭连接工厂异常", e);
            }
        });
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * HTTP连接工厂配置接口
     */
    public interface HttpConnectionFactoryConfig extends ConnectionFactoryConfig {
        // 可以扩展HTTP特有的配置
    }

    /**
     * HTTP连接工厂统计信息接口
     */
    public interface HttpConnectionFactoryStats extends ConnectionFactoryStats {
        void incrementConnectionsCreated();
        void incrementConnectionsFailed();
        void recordConnectionTime(long duration);
    }

    /**
     * 默认HTTP连接工厂配置
     */
    private static class DefaultHttpConnectionFactoryConfig implements HttpConnectionFactoryConfig {
        @Override
        public String getName() {
            return "HttpConnectionFactory";
        }

        @Override
        public Map<String, Object> getProperties() {
            Map<String, Object> props = new ConcurrentHashMap<>();
            props.put("protocol", "HTTP");
            props.put("version", "1.1");
            return props;
        }
    }

    /**
     * HTTP连接工厂统计信息实现
     */
    private static class DefaultHttpConnectionFactoryStats implements HttpConnectionFactoryStats {
        private final AtomicLong connectionsCreated = new AtomicLong(0);
        private final AtomicLong connectionsFailed = new AtomicLong(0);
        private final AtomicLong totalConnectionTime = new AtomicLong(0);
        private final AtomicLong connectionTimeCount = new AtomicLong(0);

        @Override
        public void incrementConnectionsCreated() {
            connectionsCreated.incrementAndGet();
        }

        @Override
        public void incrementConnectionsFailed() {
            connectionsFailed.incrementAndGet();
        }

        @Override
        public void recordConnectionTime(long duration) {
            totalConnectionTime.addAndGet(duration);
            connectionTimeCount.incrementAndGet();
        }

        @Override
        public long getConnectionsCreated() {
            return connectionsCreated.get();
        }

        @Override
        public long getConnectionsFailed() {
            return connectionsFailed.get();
        }

        @Override
        public double getAverageConnectionTime() {
            long count = connectionTimeCount.get();
            return count > 0 ? (double) totalConnectionTime.get() / count : 0.0;
        }

        @Override
        public double getSuccessRate() {
            long total = connectionsCreated.get() + connectionsFailed.get();
            return total > 0 ? (double) connectionsCreated.get() / total : 0.0;
        }
    }
} 