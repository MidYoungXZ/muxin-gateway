package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.HttpEndpointAddress;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty客户端连接实现
 * 基于Netty ChannelHandlerContext的客户端连接
 * 
 * @author muxin
 * @since 2.0
 */
@Slf4j
public class NettyClientConnection implements ClientConnection {

    private static final AttributeKey<String> CONNECTION_ID_KEY = AttributeKey.valueOf("CONNECTION_ID");
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    
    private final String connectionId;
    private final ChannelHandlerContext ctx;
    private final Protocol protocol;
    private final EndpointAddress localAddress;
    private final EndpointAddress remoteAddress;
    private final Map<String, Object> attributes;
    private final List<ConnectionListener> listeners;
    private volatile ConnectionStatus status;
    private final long createTime;
    private volatile long lastActiveTime;
    
    // 统计信息
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong bytesSent = new AtomicLong(0);
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public NettyClientConnection(ChannelHandlerContext ctx, EndpointAddress target, Protocol protocol) {
        this.connectionId = generateConnectionId();
        this.ctx = ctx;
        this.protocol = protocol;
        this.localAddress = createEndpointAddress(ctx.channel().localAddress());
        this.remoteAddress = target;
        this.attributes = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.status = ConnectionStatus.CONNECTED;
        this.createTime = System.currentTimeMillis();
        this.lastActiveTime = createTime;
        
        // 在Channel中存储连接ID
        ctx.channel().attr(CONNECTION_ID_KEY).set(connectionId);
        
        log.debug("[NettyClientConnection] 创建客户端连接: {} -> {}", localAddress, remoteAddress);
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public EndpointAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public EndpointAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public ConnectionStatus getStatus() {
        return status;
    }

    @Override
    public boolean isActive() {
        return ctx.channel().isActive() && status == ConnectionStatus.CONNECTED;
    }

    @Override
    public CompletableFuture<Void> close() {
        if (status == ConnectionStatus.DISCONNECTED || status == ConnectionStatus.DISCONNECTING) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.debug("[NettyClientConnection] 关闭客户端连接: {}", connectionId);
        
        status = ConnectionStatus.DISCONNECTING;
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        ChannelFuture closeFuture = ctx.close();
        closeFuture.addListener(channelFuture -> {
            status = ConnectionStatus.DISCONNECTED;
            
            if (channelFuture.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(channelFuture.cause());
            }
        });
        
        return future;
    }

    @Override
    public CompletableFuture<Void> send(Message message) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接未激活，无法发送消息: " + connectionId)
            );
        }
        
        if (message == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("消息不能为空")
            );
        }
        
        log.debug("[NettyClientConnection] 发送消息: {} -> {}", connectionId, message.getMessageId());
        updateLastActiveTime();
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // 这里应该根据协议转换消息，简化实现直接发送
        ChannelFuture writeFuture = ctx.writeAndFlush(message);
        writeFuture.addListener(channelFuture -> {
            if (channelFuture.isSuccess()) {
                messagesSent.incrementAndGet();
                future.complete(null);
            } else {
                errorCount.incrementAndGet();
                future.completeExceptionally(channelFuture.cause());
            }
        });
        
        return future;
    }

    @Override
    public CompletableFuture<Message> sendAndReceive(Message message, Duration timeout) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接未激活，无法发送消息: " + connectionId)
            );
        }
        
        log.debug("[NettyClientConnection] 发送并等待响应: {} -> {}", connectionId, message.getMessageId());
        
        CompletableFuture<Message> future = new CompletableFuture<>();
        
        // 发送消息
        send(message).thenRun(() -> {
            // 这里应该实现请求-响应匹配逻辑，简化实现
            future.completeExceptionally(new UnsupportedOperationException("简化实现暂不支持请求-响应模式"));
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });
        
        return future;
    }

    @Override
    public long getCreatedTime() {
        return createTime;
    }

    @Override
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    @Override
    public void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    @Override
    public ClientConnectionStatistics getStatistics() {
        return new NettyClientConnectionStatistics();
    }

    @Override
    public void addListener(ConnectionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            log.debug("[NettyClientConnection] 添加连接监听器: {} -> {}", 
                listener.getClass().getSimpleName(), connectionId);
        }
    }

    @Override
    public void removeListener(ConnectionListener listener) {
        if (listeners.remove(listener)) {
            log.debug("[NettyClientConnection] 移除连接监听器: {} -> {}", 
                listener.getClass().getSimpleName(), connectionId);
        }
    }

    @Override
    public List<ConnectionListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (key != null) {
            if (value != null) {
                attributes.put(key, value);
            } else {
                attributes.remove(key);
            }
        }
    }

    @Override
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    // ClientConnection特有方法实现
    @Override
    public boolean isPoolable() {
        Boolean poolable = getAttribute("poolable", Boolean.class);
        return poolable != null ? poolable : true;
    }

    @Override
    public void setPoolable(boolean poolable) {
        setAttribute("poolable", poolable);
    }

    @Override
    public long getMaxIdleTime() {
        Long maxIdleTime = getAttribute("maxIdleTime", Long.class);
        return maxIdleTime != null ? maxIdleTime : -1L;
    }

    @Override
    public void setMaxIdleTime(long maxIdleTimeMs) {
        setAttribute("maxIdleTime", maxIdleTimeMs);
    }

    @Override
    public void setWeight(int weight) {
        setAttribute("weight", weight);
    }

    @Override
    public int getWeight() {
        Integer weight = getAttribute("weight", Integer.class);
        return weight != null ? weight : 1;
    }

    @Override
    public CompletableFuture<Boolean> ping() {
        return ping(Duration.ofSeconds(5));
    }

    @Override
    public CompletableFuture<Boolean> ping(Duration timeout) {
        if (!isActive()) {
            return CompletableFuture.completedFuture(false);
        }
        
        // 简化实现：检查channel是否活跃
        try {
            boolean isHealthy = ctx.channel().isActive() && ctx.channel().isWritable();
            return CompletableFuture.completedFuture(isHealthy);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public int getActiveRequestCount() {
        Integer activeRequests = getAttribute("activeRequests", Integer.class);
        return activeRequests != null ? activeRequests : 0;
    }

    @Override
    public int getMaxConcurrentRequests() {
        Integer maxRequests = getAttribute("maxConcurrentRequests", Integer.class);
        return maxRequests != null ? maxRequests : -1;
    }

    @Override
    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        setAttribute("maxConcurrentRequests", maxConcurrentRequests);
    }

    /**
     * 获取Netty Channel上下文
     */
    public ChannelHandlerContext getChannelContext() {
        return ctx;
    }

    /**
     * 标记连接错误
     */
    public void markError(Throwable error) {
        if (status != ConnectionStatus.DISCONNECTED) {
            status = ConnectionStatus.ERROR;
            errorCount.incrementAndGet();
        }
    }

    /**
     * 记录接收到的消息
     */
    public void recordMessageReceived(Message message) {
        messagesReceived.incrementAndGet();
        updateLastActiveTime();
    }

    /**
     * 生成连接ID
     */
    private String generateConnectionId() {
        return "client-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 创建端点地址
     */
    private EndpointAddress createEndpointAddress(java.net.SocketAddress socketAddress) {
        if (socketAddress instanceof java.net.InetSocketAddress) {
            java.net.InetSocketAddress inetAddr = (java.net.InetSocketAddress) socketAddress;
            String uri = "http://" + inetAddr.getHostString() + ":" + inetAddr.getPort();
            return new HttpEndpointAddress(uri);
        }
        return new HttpEndpointAddress("http://unknown:0");
    }

    @Override
    public String toString() {
        return String.format("NettyClientConnection{id='%s', protocol=%s, local=%s, remote=%s, status=%s}", 
            connectionId, protocol.getName(), localAddress, remoteAddress, status);
    }

    /**
     * Netty客户端连接统计信息实现
     */
    private class NettyClientConnectionStatistics implements ClientConnectionStatistics {

        @Override
        public long getRequestsSent() {
            return messagesSent.get();
        }

        @Override
        public long getResponsesReceived() {
            return messagesReceived.get();
        }

        @Override
        public long getTimeoutRequests() {
            return 0; // 简化实现
        }

        @Override
        public double getAverageResponseTime() {
            return 0.0; // 简化实现
        }

        @Override
        public int getPeakConcurrentRequests() {
            return 1; // 简化实现
        }

        @Override
        public long getPoolUsageCount() {
            return 0; // 简化实现
        }

        @Override
        public long getBytesSent() {
            return bytesSent.get();
        }

        @Override
        public long getBytesReceived() {
            return bytesReceived.get();
        }

        @Override
        public long getMessagesSent() {
            return messagesSent.get();
        }

        @Override
        public long getMessagesReceived() {
            return messagesReceived.get();
        }

        @Override
        public long getErrorCount() {
            return errorCount.get();
        }

        @Override
        public long getDuration() {
            return System.currentTimeMillis() - createTime;
        }

        @Override
        public double getAverageLatency() {
            return 0.0; // 简化实现
        }
    }
} 