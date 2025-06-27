package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.core.common.Repository;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.HttpEndpointAddress;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty服务器端连接实现
 * 基于Netty ChannelHandlerContext的服务器端连接
 * 
 * @author muxin
 * @since 2.0
 */
@Slf4j
public class NettyServerConnection implements ServerConnection, Connection {

    private static final AttributeKey<String> CONNECTION_ID_KEY = AttributeKey.valueOf("CONNECTION_ID");
    
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
    private volatile boolean keepAlive = true;
    private long readTimeout = -1;
    private long writeTimeout = -1;
    
    // 统计信息
    private final AtomicLong requestsReceived = new AtomicLong(0);
    private final AtomicLong responsesSent = new AtomicLong(0);
    private final AtomicLong bytesSent = new AtomicLong(0);
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public NettyServerConnection(ChannelHandlerContext ctx, Protocol protocol) {
        this.connectionId = generateConnectionId();
        this.ctx = ctx;
        this.protocol = protocol;
        this.localAddress = createEndpointAddress(ctx.channel().localAddress());
        this.remoteAddress = createEndpointAddress(ctx.channel().remoteAddress());
        this.attributes = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.status = ConnectionStatus.CONNECTED;
        this.createTime = System.currentTimeMillis();
        this.lastActiveTime = createTime;
        
        // 在Channel中存储连接ID
        ctx.channel().attr(CONNECTION_ID_KEY).set(connectionId);
        
        log.debug("[NettyServerConnection] 创建服务器端连接: {} -> {}", remoteAddress, localAddress);
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
        
        log.debug("[NettyServerConnection] 关闭服务器端连接: {}", connectionId);
        
        status = ConnectionStatus.DISCONNECTING;
        notifyListeners(listener -> listener.onStatusChanged(this, ConnectionStatus.CONNECTED, status));
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        ChannelFuture closeFuture = ctx.close();
        closeFuture.addListener(channelFuture -> {
            status = ConnectionStatus.DISCONNECTED;
            notifyListeners(listener -> listener.onConnectionClosed(this));
            notifyListeners(listener -> listener.onStatusChanged(this, ConnectionStatus.DISCONNECTING, status));
            
            if (channelFuture.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(channelFuture.cause());
            }
        });
        
        return future;
    }

    // Connection接口的send方法实现（服务器端连接不支持主动发送）
    @Override
    public CompletableFuture<Message> send(Message message) {
        return CompletableFuture.failedFuture(
            new UnsupportedOperationException("服务器端连接不支持主动发送消息")
        );
    }

    @Override
    public CompletableFuture<Void> writeResponse(Object protocolSpecificResponse) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接未激活，无法写入响应: " + connectionId)
            );
        }
        
        if (protocolSpecificResponse == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("响应对象不能为空")
            );
        }
        
        log.debug("[NettyServerConnection] 写入响应到连接: {}", connectionId);
        updateLastActiveTime();
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        ChannelFuture writeFuture = ctx.writeAndFlush(protocolSpecificResponse);
        writeFuture.addListener(channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.debug("[NettyServerConnection] 响应写入成功: {}", connectionId);
                responsesSent.incrementAndGet();
                future.complete(null);
            } else {
                log.error("[NettyServerConnection] 响应写入失败: " + connectionId, channelFuture.cause());
                errorCount.incrementAndGet();
                notifyListeners(listener -> listener.onConnectionError(this, channelFuture.cause()));
                future.completeExceptionally(channelFuture.cause());
            }
        });
        
        return future;
    }

    @Override
    public CompletableFuture<Void> writeRawData(byte[] data) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接未激活，无法写入数据: " + connectionId)
            );
        }
        
        if (data == null || data.length == 0) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.debug("[NettyServerConnection] 写入原始数据到连接: {} ({}字节)", connectionId, data.length);
        updateLastActiveTime();
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        ChannelFuture writeFuture = ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(data));
        writeFuture.addListener(channelFuture -> {
            if (channelFuture.isSuccess()) {
                bytesSent.addAndGet(data.length);
                future.complete(null);
            } else {
                errorCount.incrementAndGet();
                notifyListeners(listener -> listener.onConnectionError(this, channelFuture.cause()));
                future.completeExceptionally(channelFuture.cause());
            }
        });
        
        return future;
    }

    @Override
    public CompletableFuture<Void> flush() {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接未激活，无法刷新: " + connectionId)
            );
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        ctx.flush();
        future.complete(null); // flush是同步操作
        
        return future;
    }

    @Override
    public <T> T getProtocolContext(Class<T> contextType) {
        if (contextType.isInstance(ctx)) {
            return contextType.cast(ctx);
        }
        return null;
    }

    @Override
    public boolean supportsPersistentConnection() {
        return true; // Netty支持持久连接
    }

    @Override
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    @Override
    public long getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(long timeoutMs) {
        this.readTimeout = timeoutMs;
    }

    @Override
    public long getWriteTimeout() {
        return writeTimeout;
    }

    @Override
    public void setWriteTimeout(long timeoutMs) {
        this.writeTimeout = timeoutMs;
    }

    @Override
    public boolean isReadable() {
        return isActive() && ctx.channel().isOpen();
    }

    @Override
    public boolean isWritable() {
        return isActive() && ctx.channel().isWritable();
    }

    @Override
    public int getWriteBufferSize() {
        return (int) ctx.channel().bytesBeforeUnwritable();
    }

    @Override
    public int getWriteBufferHighWaterMark() {
        return ctx.channel().config().getWriteBufferHighWaterMark();
    }

    @Override
    public int getWriteBufferLowWaterMark() {
        return ctx.channel().config().getWriteBufferLowWaterMark();
    }

    @Override
    public String getUserAgent() {
        return getAttribute("User-Agent", String.class);
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        // 检查是否使用SSL/TLS
        if (ctx.pipeline().get("ssl") != null) {
            return SecurityLevel.HIGH;
        }
        return SecurityLevel.NONE;
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
    public ServerConnectionStatistics getStatistics() {
        return new NettyServerConnectionStatistics();
    }

    @Override
    public void addListener(ConnectionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            log.debug("[NettyServerConnection] 添加连接监听器: {} -> {}", 
                listener.getClass().getSimpleName(), connectionId);
        }
    }

    @Override
    public void removeListener(ConnectionListener listener) {
        if (listeners.remove(listener)) {
            log.debug("[NettyServerConnection] 移除连接监听器: {} -> {}", 
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

    // Repository接口实现（来自Connection接口）
    @Override
    public ConnectionListener save(ConnectionListener entity) {
        if (entity != null && !listeners.contains(entity)) {
            listeners.add(entity);
        }
        return entity;
    }

    @Override
    public void removeByUniqueCode(String uniqueCode) {
        listeners.removeIf(listener -> uniqueCode.equals(listener.id()));
    }

    @Override
    public ConnectionListener findByUniqueCode(String uniqueCode) {
        return listeners.stream()
            .filter(listener -> uniqueCode.equals(listener.id()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<ConnectionListener> findAll() {
        return new ArrayList<>(listeners);
    }

    /**
     * 获取Netty Channel上下文
     */
    public ChannelHandlerContext getChannelContext() {
        return ctx;
    }

    /**
     * 获取连接创建时间
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * 标记连接错误
     */
    public void markError(Throwable error) {
        if (status != ConnectionStatus.DISCONNECTED) {
            ConnectionStatus oldStatus = status;
            status = ConnectionStatus.ERROR;
            errorCount.incrementAndGet();
            notifyListeners(listener -> listener.onConnectionError(this, error));
            notifyListeners(listener -> listener.onStatusChanged(this, oldStatus, status));
        }
    }

    /**
     * 记录接收到的请求
     */
    public void recordRequestReceived() {
        requestsReceived.incrementAndGet();
        updateLastActiveTime();
    }

    /**
     * 生成连接ID
     */
    private String generateConnectionId() {
        return "server-" + UUID.randomUUID().toString().substring(0, 8);
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

    /**
     * 通知所有监听器
     */
    public void notifyListeners(java.util.function.Consumer<ConnectionListener> action) {
        for (ConnectionListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                log.error("[NettyServerConnection] 通知监听器发生错误: " + listener.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("NettyServerConnection{id='%s', protocol=%s, local=%s, remote=%s, status=%s}", 
            connectionId, protocol.getName(), localAddress, remoteAddress, status);
    }

    /**
     * Netty服务器连接统计信息实现
     */
    private class NettyServerConnectionStatistics implements ServerConnectionStatistics {

        @Override
        public long getRequestsReceived() {
            return requestsReceived.get();
        }

        @Override
        public long getResponsesSent() {
            return responsesSent.get();
        }

        @Override
        public int getPeakConcurrentRequests() {
            return 1; // 简化实现
        }

        @Override
        public double getAverageRequestProcessingTime() {
            return 0.0; // 简化实现
        }

        @Override
        public long getBufferOverflowCount() {
            return 0; // 简化实现
        }

        @Override
        public long getConnectionDuration() {
            return System.currentTimeMillis() - createTime;
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
            return responsesSent.get();
        }

        @Override
        public long getMessagesReceived() {
            return requestsReceived.get();
        }

        @Override
        public long getErrorCount() {
            return errorCount.get();
        }

        @Override
        public long getDuration() {
            return getConnectionDuration();
        }

        @Override
        public double getAverageLatency() {
            return 0.0; // 简化实现
        }
    }
} 