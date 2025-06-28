package com.muxin.gateway.core.plus.connect.http;

import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ConnectionListener;
import com.muxin.gateway.core.plus.connect.ConnectionStatus;
import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.message.http.HttpMessage;
import com.muxin.gateway.core.plus.message.http.HttpHeaders;
import com.muxin.gateway.core.plus.message.http.HttpBody;
import com.muxin.gateway.core.plus.message.http.HttpMetadata;
import com.muxin.gateway.core.plus.message.MessageType;
import com.muxin.gateway.core.plus.node.EndpointAddress;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 基于Netty的HTTP客户端连接实现
 * 支持连接池管理和健康检查
 * 
 * @author muxin
 */
@Slf4j
public class HttpClientConnection implements ClientConnection {
    
    private final String connectionId;
    private final Channel channel;
    private final EndpointAddress localAddress;
    private final EndpointAddress remoteAddress;
    private final Protocol protocol;
    private final HttpConnectionPool ownerPool;
    
    // 连接状态管理
    private final AtomicReference<ConnectionStatus> status;
    private final AtomicBoolean inUse;
    private final AtomicLong lastUsedTime;
    private final AtomicLong createdTime;
    private final AtomicLong totalRequests;
    private final AtomicLong totalFailures;
    
    // 连接属性
    private final Map<String, Object> attributes;
    private final Map<String, ConnectionListener> listeners;
    
    // 当前请求处理
    private volatile CompletableFuture<Message> currentRequest;
    
    public HttpClientConnection(String connectionId, 
                               Channel channel,
                               EndpointAddress localAddress,
                               EndpointAddress remoteAddress,
                               Protocol protocol,
                               HttpConnectionPool ownerPool) {
        this.connectionId = connectionId;
        this.channel = channel;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.protocol = protocol;
        this.ownerPool = ownerPool;
        
        this.status = new AtomicReference<>(ConnectionStatus.CONNECTING);
        this.inUse = new AtomicBoolean(false);
        this.lastUsedTime = new AtomicLong(System.currentTimeMillis());
        this.createdTime = new AtomicLong(System.currentTimeMillis());
        this.totalRequests = new AtomicLong(0);
        this.totalFailures = new AtomicLong(0);
        
        this.attributes = new ConcurrentHashMap<>();
        this.listeners = new ConcurrentHashMap<>();
        
        // 设置为已连接状态
        if (channel != null && channel.isActive()) {
            this.status.set(ConnectionStatus.CONNECTED);
        }
        
        log.debug("[HttpClientConnection] 创建HTTP客户端连接: {} -> {}", 
            localAddress != null ? localAddress.toUri() : "unknown",
            remoteAddress != null ? remoteAddress.toUri() : "unknown");
    }
    
    @Override
    public String getConnectionId() {
        return connectionId;
    }
    
    @Override
    public Protocol getProtocol() {
        return protocol;
    }
    
    public EndpointAddress getLocalAddress() {
        return localAddress;
    }
    
    public EndpointAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    public ConnectionStatus getStatus() {
        return status.get();
    }
    
    @Override
    public CompletableFuture<Message> send(Message message) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接不可用: " + connectionId));
        }
        
        if (currentRequest != null && !currentRequest.isDone()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接正在处理其他请求"));
        }
        
        return doSend(message);
    }
    
    private CompletableFuture<Message> doSend(Message message) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        currentRequest = future;
        
        try {
            // 更新使用时间和计数
            lastUsedTime.set(System.currentTimeMillis());
            totalRequests.incrementAndGet();
            
            // 转换Message为HTTP请求
            FullHttpRequest httpRequest = convertToHttpRequest(message);
            
            // 设置响应处理器
            setupResponseHandler(future);
            
            // 发送请求
            channel.writeAndFlush(httpRequest).addListener(channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    totalFailures.incrementAndGet();
                    future.completeExceptionally(new RuntimeException("发送请求失败", channelFuture.cause()));
                }
            });
            
            log.debug("[HttpClientConnection] 发送HTTP请求: {} -> {}", 
                message.getMessageId(), remoteAddress.toUri());
                
        } catch (Exception e) {
            totalFailures.incrementAndGet();
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    private FullHttpRequest convertToHttpRequest(Message message) {
        // 从消息元数据获取HTTP信息
        HttpMetadata metadata = (HttpMetadata) message.getMetadata();
        
        // 构建HTTP方法和URI
        String method = metadata != null ? metadata.getMethod() : "GET";
        String uri = metadata != null ? metadata.getAttribute("uri", String.class) : "/";
        if (uri == null) uri = "/";
        
        // 创建HTTP请求
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.valueOf(method),
            uri
        );
        
        // 设置请求头
        if (message.getHeaders() != null) {
            Map<String, Object> headers = message.getHeaders().asMap();
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    httpRequest.headers().set(entry.getKey(), entry.getValue().toString());
                }
            }
        }
        
        // 设置请求体
        if (message.getBody() != null && !message.getBody().isEmpty()) {
            byte[] bodyBytes = message.getBody().getBytes();
            httpRequest.content().writeBytes(bodyBytes);
            httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, bodyBytes.length);
        } else {
            httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        }
        
        // 设置默认头部
        if (!httpRequest.headers().contains(HttpHeaderNames.HOST)) {
            httpRequest.headers().set(HttpHeaderNames.HOST, remoteAddress.getHost() + ":" + remoteAddress.getPort());
        }
        if (!httpRequest.headers().contains(HttpHeaderNames.CONNECTION)) {
            httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        
        return httpRequest;
    }
    
    private void setupResponseHandler(CompletableFuture<Message> future) {
        // 注册响应处理器到Channel的pipeline中
        // 这里简化实现，实际应该使用专门的响应处理器
        channel.pipeline().addLast("responseHandler", new HttpResponseHandler(future));
    }
    
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                status.set(ConnectionStatus.DISCONNECTED);
                
                if (channel != null && channel.isActive()) {
                    channel.close().sync();
                }
                
                // 连接已关闭
                
                log.debug("[HttpClientConnection] 关闭HTTP客户端连接: {}", connectionId);
                
            } catch (Exception e) {
                log.error("[HttpClientConnection] 关闭连接异常: " + connectionId, e);
            }
        });
    }
    
    @Override
    public boolean isActive() {
        return channel != null && 
               channel.isActive() && 
               status.get() == ConnectionStatus.CONNECTED;
    }
    
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
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    // ClientConnection接口方法
    
    @Override
    public boolean isHealthy() {
        // 1. 检查网络连接状态
        if (!isActive()) {
            return false;
        }
        
        // 2. 检查空闲时间
        long idleTime = System.currentTimeMillis() - lastUsedTime.get();
        long maxIdleTime = ownerPool != null ? ownerPool.getConfig().getIdleTimeout().toMillis() : 300000; // 5分钟默认
        if (idleTime > maxIdleTime) {
            return false;
        }
        
        // 3. 检查连接生存时间
        long lifetime = System.currentTimeMillis() - createdTime.get();
        long maxLifetime = ownerPool != null ? ownerPool.getConfig().getMaxLifetime().toMillis() : 1800000; // 30分钟默认
        if (lifetime > maxLifetime) {
            return false;
        }
        
        // 4. 检查失败率
        long totalReqs = totalRequests.get();
        long totalFails = totalFailures.get();
        if (totalReqs > 10 && totalFails > totalReqs * 0.5) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public void markInUse() {
        inUse.set(true);
        lastUsedTime.set(System.currentTimeMillis());
    }
    
    @Override
    public void markIdle() {
        inUse.set(false);
        lastUsedTime.set(System.currentTimeMillis());
        currentRequest = null;
    }
    
    @Override
    public boolean isInUse() {
        return inUse.get();
    }
    
    @Override
    public void returnToPool() {
        if (ownerPool != null) {
            markIdle();
            ownerPool.returnConnection(this);
        }
    }
    
    @Override
    public void destroy() {
        try {
            status.set(ConnectionStatus.DISCONNECTED);
            if (channel != null && channel.isActive()) {
                channel.close();
            }
        } catch (Exception e) {
            log.error("[HttpClientConnection] 销毁连接异常: " + connectionId, e);
        }
    }
    
    @Override
    public long getTotalRequests() {
        return totalRequests.get();
    }
    
    @Override
    public long getTotalFailures() {
        return totalFailures.get();
    }
    
    // 辅助方法（非接口方法）
    
    public long getLastUsedTime() {
        return lastUsedTime.get();
    }
    
    /**
     * 获取连接统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("connectionId", connectionId);
        stats.put("createdTime", createdTime.get());
        stats.put("lastUsedTime", lastUsedTime.get());
        stats.put("totalRequests", totalRequests.get());
        stats.put("totalFailures", totalFailures.get());
        stats.put("inUse", inUse.get());
        stats.put("isActive", isActive());
        stats.put("isHealthy", isHealthy());
        return stats;
    }
    
    @Override
    public long getCreatedTime() {
        return createdTime.get();
    }
    
    @Override
    public long getLastActiveTime() {
        return lastUsedTime.get();
    }
    
    /**
     * 简单的HTTP响应处理器
     */
    private static class HttpResponseHandler extends io.netty.channel.SimpleChannelInboundHandler<FullHttpResponse> {
        
        private final CompletableFuture<Message> future;
        
        public HttpResponseHandler(CompletableFuture<Message> future) {
            this.future = future;
        }
        
        @Override
        protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
            try {
                // 转换HTTP响应为Message
                Message message = convertToMessage(response);
                future.complete(message);
                
                // 移除处理器
                ctx.pipeline().remove(this);
                
            } catch (Exception e) {
                future.completeExceptionally(e);
                ctx.pipeline().remove(this);
            }
        }
        
        @Override
        public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) throws Exception {
            future.completeExceptionally(cause);
            ctx.pipeline().remove(this);
        }
        
        private Message convertToMessage(FullHttpResponse response) {
            // 创建HTTP消息组件
            String messageId = UUID.randomUUID().toString();
            
            // 创建Headers
            HttpHeaders headers = new HttpHeaders();
            for (Map.Entry<String, String> entry : response.headers()) {
                headers.set(entry.getKey(), entry.getValue());
            }
            
            // 创建Body
            byte[] bodyBytes = new byte[response.content().readableBytes()];
            response.content().readBytes(bodyBytes);
            HttpBody body = new HttpBody(bodyBytes);
            
            // 创建Metadata
            HttpMetadata metadata = new HttpMetadata();
            metadata.setAttribute("statusCode", response.status().code());
            metadata.setAttribute("statusText", response.status().reasonPhrase());
            metadata.setAttribute("version", response.protocolVersion().text());
            
            return new HttpMessage(
                messageId,
                MessageType.RESPONSE,
                Protocol.UNIVERSAL,
                headers,
                body,
                metadata
            );
        }
    }
} 