package com.muxin.gateway.core.plus.connect.http;

import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.Connection;
import com.muxin.gateway.core.plus.connect.ConnectionFactory;
import com.muxin.gateway.core.plus.connect.ServerConnection;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.route.node.EndpointAddress;
import com.muxin.gateway.core.plus.route.node.HttpEndpointAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;

/**
 * HTTP连接工厂实现
 * 基于Netty Bootstrap创建HTTP客户端连接
 * 
 * @author muxin
 */
@Slf4j
public class HttpConnectionFactory implements ConnectionFactory {
    
    private final Protocol httpProtocol;
    private final Bootstrap bootstrap;
    private final EventLoopGroup workerGroup;
    
    // 统计信息
    private final AtomicLong connectionsCreated;
    private final AtomicLong connectionsFailed;
    private final AtomicLong totalConnectionTime;
    
    // 状态管理
    private volatile boolean running = false;
    
    public HttpConnectionFactory() {
        this.httpProtocol = new Protocol.HttpProtocol();
        
        this.connectionsCreated = new AtomicLong(0);
        this.connectionsFailed = new AtomicLong(0);
        this.totalConnectionTime = new AtomicLong(0);
        
        // 创建工作线程组
        this.workerGroup = new NioEventLoopGroup(
            Runtime.getRuntime().availableProcessors(),
            new DefaultThreadFactory("HttpClient-Worker")
        );
        
        // 配置Bootstrap
        this.bootstrap = configureBootstrap();
        
        log.info("[HttpConnectionFactory] HTTP连接工厂创建完成");
    }
    
    @Override
    public Protocol getSupportedProtocol() {
        return httpProtocol;
    }
    
    public HttpClientConnection createConnection(EndpointAddress target, Protocol protocol) {
        return createConnection(target, protocol, null);
    }
    
    @Override
    public ServerConnection createServerConnection(Object protocolContext) throws ConnectionCreationException {
        throw new ConnectionCreationException("HTTP工厂不支持创建服务器连接", httpProtocol);
    }
    
    @Override
    public CompletableFuture<ClientConnection> createClientConnection(EndpointAddress target, Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            HttpClientConnection connection = createConnection(target, httpProtocol, null);
            return (ClientConnection) connection;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> validateConnection(Connection connection) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection instanceof ClientConnection) {
                ClientConnection clientConn = (ClientConnection) connection;
                return clientConn.isActive() && clientConn.isHealthy();
            }
            return false;
        });
    }
    
    @Override
    public ConnectionHealthStatus getConnectionHealth(Connection connection) {
        if (connection instanceof ClientConnection) {
            ClientConnection clientConn = (ClientConnection) connection;
            if (clientConn.isActive() && clientConn.isHealthy()) {
                return ConnectionHealthStatus.HEALTHY;
            } else if (clientConn.isActive()) {
                return ConnectionHealthStatus.WARNING;
            } else {
                return ConnectionHealthStatus.UNHEALTHY;
            }
        }
        return ConnectionHealthStatus.UNKNOWN;
    }
    
    @Override
    public Map<String, Object> getDefaultConnectionOptions() {
        Map<String, Object> options = new ConcurrentHashMap<>();
        options.put("keepAlive", true);
        options.put("tcpNoDelay", true);
        options.put("connectTimeout", 5000);
        options.put("readTimeout", 30000);
        options.put("writeTimeout", 30000);
        return options;
    }
    
    @Override
    public ConnectionFactoryConfig getConfig() {
        return new SimpleConnectionFactoryConfig();
    }
    
    @Override
    public void updateConfig(ConnectionFactoryConfig config) {
        log.info("[HttpConnectionFactory] 更新配置: {}", config);
    }
    
    @Override
    public ConnectionFactoryStats getStats() {
        return new SimpleConnectionFactoryStats();
    }
    
    @Override
    public CompletableFuture<Void> warmup() {
        return CompletableFuture.runAsync(() -> {
            log.info("[HttpConnectionFactory] 预热完成");
        });
    }
    
    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            stop();
        });
    }
    
    @Override
    public boolean isShutdown() {
        return !running;
    }
    
    public HttpClientConnection createConnection(EndpointAddress target, 
                                               Protocol protocol,
                                               HttpConnectionPool pool) {
        if (!running) {
            throw new IllegalStateException("连接工厂未启动");
        }
        
        if (!httpProtocol.equals(protocol)) {
            throw new IllegalArgumentException("不支持的协议: " + protocol.getName());
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 连接到目标服务器
            ChannelFuture channelFuture = bootstrap.connect(target.getHost(), target.getPort());
            Channel channel = channelFuture.sync().channel();
            
            // 创建连接ID
            String connectionId = generateConnectionId(target);
            
            // 创建本地地址
            EndpointAddress localAddress = createLocalAddress(channel);
            
            // 创建HTTP客户端连接
            HttpClientConnection connection = new HttpClientConnection(
                connectionId,
                channel,
                localAddress,
                target,
                protocol,
                pool
            );
            
            // 更新统计信息
            long connectionTime = System.currentTimeMillis() - startTime;
            connectionsCreated.incrementAndGet();
            totalConnectionTime.addAndGet(connectionTime);
            
            log.debug("[HttpConnectionFactory] 创建HTTP连接成功: {} -> {} - 耗时: {}ms", 
                localAddress.toUri(), target.toUri(), connectionTime);
            
            return connection;
            
        } catch (Exception e) {
            connectionsFailed.incrementAndGet();
            long connectionTime = System.currentTimeMillis() - startTime;
            
            log.error("[HttpConnectionFactory] 创建HTTP连接失败: {} - 耗时: {}ms", 
                target.toUri(), connectionTime, e);
            
            throw new RuntimeException("创建HTTP连接失败", e);
        }
    }
    
    public ConnectionHealthStatus getHealthStatus() {
        if (!running) {
            return ConnectionHealthStatus.UNHEALTHY;
        }
        
        // 简单的健康检查：检查失败率
        long total = connectionsCreated.get() + connectionsFailed.get();
        if (total > 10) {
            double failureRate = (double) connectionsFailed.get() / total;
            if (failureRate > 0.5) {
                return ConnectionHealthStatus.WARNING;
            }
        }
        
        return ConnectionHealthStatus.HEALTHY;
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("protocol", httpProtocol.getName());
        stats.put("running", running);
        stats.put("connectionsCreated", connectionsCreated.get());
        stats.put("connectionsFailed", connectionsFailed.get());
        stats.put("averageConnectionTime", calculateAverageConnectionTime());
        stats.put("successRate", calculateSuccessRate());
        return stats;
    }
    
    @Override
    public boolean supports(Protocol protocol) {
        return httpProtocol.equals(protocol);
    }
    
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        log.info("[HttpConnectionFactory] HTTP连接工厂启动");
    }
    
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        
        // 关闭工作线程组
        try {
            workerGroup.shutdownGracefully(2, 10, TimeUnit.SECONDS).sync();
            log.info("[HttpConnectionFactory] HTTP连接工厂停止完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[HttpConnectionFactory] 停止连接工厂时被中断", e);
        }
    }
    // 私有方法
    
    private Bootstrap configureBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_RCVBUF, 64 * 1024)
            .option(ChannelOption.SO_SNDBUF, 64 * 1024)
            .handler(new HttpClientChannelInitializer());
        
        return bootstrap;
    }
    
    private String generateConnectionId(EndpointAddress target) {
        return "http-" + target.getHost() + "-" + target.getPort() + "-" + 
               UUID.randomUUID().toString().substring(0, 8);
    }
    
    private EndpointAddress createLocalAddress(Channel channel) {
        if (channel.localAddress() instanceof java.net.InetSocketAddress) {
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) channel.localAddress();
            return new HttpEndpointAddress(addr.getHostString(), addr.getPort());
        }
        return new HttpEndpointAddress("unknown", 0);
    }
    
    private double calculateAverageConnectionTime() {
        long created = connectionsCreated.get();
        return created > 0 ? (double) totalConnectionTime.get() / created : 0.0;
    }
    
    private double calculateSuccessRate() {
        long total = connectionsCreated.get() + connectionsFailed.get();
        return total > 0 ? (double) connectionsCreated.get() / total : 1.0;
    }
    
    /**
     * 简单的连接工厂配置实现
     */
    private class SimpleConnectionFactoryConfig implements ConnectionFactoryConfig {
        @Override
        public String getName() {
            return "HttpConnectionFactory";
        }
        
        @Override
        public Map<String, Object> getProperties() {
            Map<String, Object> props = new ConcurrentHashMap<>();
            props.put("protocol", httpProtocol.getName());
            props.put("running", running);
            return props;
        }
    }
    
    /**
     * 简单的连接工厂统计实现
     */
    private class SimpleConnectionFactoryStats implements ConnectionFactoryStats {
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
            return calculateAverageConnectionTime();
        }
        
        @Override
        public double getSuccessRate() {
            return calculateSuccessRate();
        }
    }
    
    /**
     * HTTP客户端Channel初始化器
     */
    private class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel> {
        
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            
            // 空闲状态处理器
            pipeline.addLast(new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
            
            // HTTP编解码器
            pipeline.addLast(new HttpClientCodec());
            
            // HTTP消息聚合器
            pipeline.addLast(new HttpObjectAggregator(10 * 1024 * 1024)); // 10MB
            
            // 压缩处理器
            pipeline.addLast(new HttpContentDecompressor());
            
            log.debug("[HttpConnectionFactory] HTTP客户端Channel初始化完成: {}", 
                ch.remoteAddress());
        }
    }
    
    /**
     * 简化的默认配置实现
     */
    private static class DefaultConfig implements ConnectionFactoryConfig {
        private final Map<String, Object> properties = new HashMap<>();
        
        @Override
        public String getName() {
            return "default-http-config";
        }
        
        @Override
        public Map<String, Object> getProperties() {
            return new HashMap<>(properties);
        }
    }
    
    /**
     * 简化的默认统计实现
     */
    private static class DefaultStats implements ConnectionFactoryStats {
        private final AtomicLong connectionsCreated = new AtomicLong(0);
        private final AtomicLong connectionsFailed = new AtomicLong(0);
        private final AtomicLong totalConnectionTime = new AtomicLong(0);
        
        public void incrementCreated() {
            connectionsCreated.incrementAndGet();
        }
        
        public void incrementFailed() {
            connectionsFailed.incrementAndGet();
        }
        
        public void addConnectionTime(long time) {
            totalConnectionTime.addAndGet(time);
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
            long created = getConnectionsCreated();
            return created > 0 ? (double) totalConnectionTime.get() / created : 0.0;
        }
        
        @Override
        public double getSuccessRate() {
            long total = getConnectionsCreated();
            long failed = getConnectionsFailed();
            return total > 0 ? (double) (total - failed) / total : 0.0;
        }
    }
}