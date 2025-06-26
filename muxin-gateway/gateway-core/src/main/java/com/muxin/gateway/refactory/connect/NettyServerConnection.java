package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.node.EndpointAddress;
import com.muxin.gateway.refactory.node.HttpEndpointAddress;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty服务器端连接实现
 * 用于处理入站HTTP连接，不再负责协议转换（由ProtocolAdapter负责）
 * 
 * @author muxin
 */
@Slf4j
public class NettyServerConnection implements Connection {
    
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    
    private final String connectionId;
    private final Protocol protocol;
    private final EndpointAddress localAddress;
    private final EndpointAddress remoteAddress;
    private final ChannelHandlerContext ctx;
    private final Map<String, Object> attributes;
    private final List<ConnectionListener> listeners;
    
    private volatile ConnectionStatus status;
    private volatile long lastActiveTime;
    
    public NettyServerConnection(ChannelHandlerContext ctx, Protocol protocol) {
        this.connectionId = "server-conn-" + ID_GENERATOR.incrementAndGet();
        this.ctx = ctx;
        this.protocol = protocol;
        this.attributes = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.status = ConnectionStatus.CONNECTED;
        this.lastActiveTime = System.currentTimeMillis();
        
        // 解析地址信息
        this.localAddress = parseLocalAddress(ctx);
        this.remoteAddress = parseRemoteAddress(ctx);
        
        log.debug("[NettyServerConnection] 创建服务器连接: {} - {} -> {}", 
            connectionId, remoteAddress.toUri(), localAddress.toUri());
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
    public CompletableFuture<Message> send(Message message) {
        // 服务器连接不支持主动发送，只能响应
        return CompletableFuture.failedFuture(
            new UnsupportedOperationException("服务器连接不支持主动发送消息"));
    }
    
    /**
     * 写入已转换的协议特定响应到客户端
     * 注意：协议转换应该在调用此方法之前完成（由ProtocolAdapter负责）
     * 
     * @param protocolResponse 已转换的协议特定响应对象（如FullHttpResponse）
     * @return 写入操作的CompletableFuture
     */
    public CompletableFuture<Void> writeProtocolResponse(Object protocolResponse) {
        if (protocolResponse == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("协议响应对象不能为空"));
        }
        
        try {
            updateLastActiveTime();
            
            // 异步写入并返回CompletableFuture
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            ctx.writeAndFlush(protocolResponse).addListener(channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.debug("[NettyServerConnection] 协议响应写入成功: {}", connectionId);
                    future.complete(null);
                } else {
                    log.error("[NettyServerConnection] 协议响应写入失败: {}", connectionId, channelFuture.cause());
                    recordError();
                    future.completeExceptionally(channelFuture.cause());
                }
            });
            
            return future;
            
        } catch (Exception e) {
            log.error("[NettyServerConnection] 写入协议响应时发生异常: {}", connectionId, e);
            recordError();
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * @deprecated 此方法违反了单一职责原则，协议转换应由ProtocolAdapter负责
     * 请使用 {@link #writeProtocolResponse(Object)} 方法，并在调用前使用ProtocolAdapter进行转换
     */
    @Deprecated
    public CompletableFuture<Void> writeResponse(Message response) {
        log.warn("[NettyServerConnection] writeResponse(Message) 方法已废弃，请使用ProtocolAdapter进行协议转换后调用writeProtocolResponse()");
        return CompletableFuture.failedFuture(
            new UnsupportedOperationException("writeResponse(Message) 方法已废弃，请使用ProtocolAdapter进行协议转换"));
    }
    
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                setStatus(ConnectionStatus.DISCONNECTING);
                
                log.debug("[NettyServerConnection] 关闭连接: {}", connectionId);
                
                // 通知监听器
                notifyListeners(listener -> listener.onConnectionClosed(this));
                
                // 关闭Netty channel
                if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
                    ctx.close();
                }
                
                setStatus(ConnectionStatus.DISCONNECTED);
                
            } catch (Exception e) {
                log.error("[NettyServerConnection] 关闭连接时发生异常: {}", connectionId, e);
                setStatus(ConnectionStatus.ERROR);
            }
        });
    }
    
    @Override
    public boolean isActive() {
        return status == ConnectionStatus.CONNECTED && 
               ctx != null && 
               ctx.channel() != null && 
               ctx.channel().isActive();
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
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
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        
        if (type.isInstance(value)) {
            return (T) value;
        }
        
        throw new ClassCastException("无法将属性 " + key + " 转换为类型 " + type.getName());
    }
    
    /**
     * 获取Netty上下文（用于协议特定操作）
     * 
     * @return ChannelHandlerContext
     */
    public ChannelHandlerContext getNettyContext() {
        return ctx;
    }
    
    /**
     * 获取最后活跃时间
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }
    
    /**
     * 设置连接状态
     */
    private void setStatus(ConnectionStatus newStatus) {
        ConnectionStatus oldStatus = this.status;
        this.status = newStatus;
        
        if (oldStatus != newStatus) {
            log.debug("[NettyServerConnection] 连接状态变更: {} {} -> {}", 
                connectionId, oldStatus, newStatus);
            notifyListeners(listener -> listener.onStatusChanged(this, oldStatus, newStatus));
        }
    }
    
    /**
     * 记录错误
     */
    private void recordError() {
        // 统计错误信息
        setAttribute("errorCount", 
            ((Integer) attributes.getOrDefault("errorCount", 0)) + 1);
    }
    
    /**
     * 解析本地地址
     */
    private EndpointAddress parseLocalAddress(ChannelHandlerContext ctx) {
        if (ctx != null && ctx.channel() != null && ctx.channel().localAddress() instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) ctx.channel().localAddress();
            String uri = "http://" + addr.getHostString() + ":" + addr.getPort();
            return new HttpEndpointAddress(uri);
        }
        return new HttpEndpointAddress("http://localhost:8080");
    }
    
    /**
     * 解析远程地址
     */
    private EndpointAddress parseRemoteAddress(ChannelHandlerContext ctx) {
        if (ctx != null && ctx.channel() != null && ctx.channel().remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
            String uri = "http://" + addr.getHostString() + ":" + addr.getPort();
            return new HttpEndpointAddress(uri);
        }
        return new HttpEndpointAddress("http://unknown:0");
    }
    
    // Repository接口实现（继承自Connection）
    @Override
    public ConnectionListener save(ConnectionListener entity) {
        if (entity != null) {
            listeners.add(entity);
            return entity;
        }
        return null;
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
        return List.copyOf(listeners);
    }
    
    @Override
    public String toString() {
        return "NettyServerConnection{" +
                "connectionId='" + connectionId + '\'' +
                ", protocol=" + protocol +
                ", localAddress=" + localAddress +
                ", remoteAddress=" + remoteAddress +
                ", status=" + status +
                ", lastActiveTime=" + lastActiveTime +
                '}';
    }
} 