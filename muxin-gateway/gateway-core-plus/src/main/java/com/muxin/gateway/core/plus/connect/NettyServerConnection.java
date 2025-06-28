package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于Netty的服务端连接实现
 * 用于处理客户端发送给网关的入站请求
 * 
 * @author muxin
 */
@Slf4j
public class NettyServerConnection implements ServerConnection {
    
    private final String connectionId;
    private final ChannelHandlerContext ctx;
    private final Protocol protocol;
    private final long createdTime;
    private final AtomicLong lastActiveTime;
    private final AtomicLong totalResponses;
    private final AtomicLong errorResponses;
    private final Map<String, Object> attributes;
    
    public NettyServerConnection(ChannelHandlerContext ctx, Protocol protocol) {
        this.connectionId = generateConnectionId();
        this.ctx = ctx;
        this.protocol = protocol;
        this.createdTime = System.currentTimeMillis();
        this.lastActiveTime = new AtomicLong(System.currentTimeMillis());
        this.totalResponses = new AtomicLong(0);
        this.errorResponses = new AtomicLong(0);
        this.attributes = new ConcurrentHashMap<>();
        
        log.debug("[NettyServerConnection] 创建服务端连接: {} - 客户端地址: {}", 
            connectionId, getClientAddress());
    }
    
    @Override
    public String getConnectionId() {
        return connectionId;
    }
    
    @Override
    public boolean isActive() {
        return ctx != null && ctx.channel() != null && ctx.channel().isActive();
    }
    
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
                    ctx.close().sync();
                }
                log.debug("[NettyServerConnection] 服务端连接关闭: {}", connectionId);
            } catch (Exception e) {
                log.error("[NettyServerConnection] 关闭连接异常: " + connectionId, e);
            }
        });
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
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("connectionId", connectionId);
        stats.put("createdTime", createdTime);
        stats.put("lastActiveTime", lastActiveTime.get());
        stats.put("totalResponses", totalResponses.get());
        stats.put("errorResponses", errorResponses.get());
        stats.put("isActive", isActive());
        stats.put("clientAddress", getClientAddress());
        return stats;
    }
    
    @Override
    public long getCreatedTime() {
        return createdTime;
    }
    
    @Override
    public long getLastActiveTime() {
        return lastActiveTime.get();
    }
    
    @Override
    public Protocol getProtocol() {
        return protocol;
    }
    
    // ServerConnection特有方法
    
    @Override
    public CompletableFuture<Void> sendResponse(Message response) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接不可用: " + connectionId));
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                // 更新活跃时间和计数
                lastActiveTime.set(System.currentTimeMillis());
                totalResponses.incrementAndGet();
                
                // 转换Message为HTTP响应
                FullHttpResponse httpResponse = convertToHttpResponse(response);
                
                // 发送响应
                ctx.writeAndFlush(httpResponse).addListener(future -> {
                    if (future.isSuccess()) {
                        log.debug("[NettyServerConnection] 响应发送成功: {} -> {}", 
                            response.getMessageId(), getClientAddress());
                    } else {
                        log.error("[NettyServerConnection] 响应发送失败: {} -> {}", 
                            response.getMessageId(), getClientAddress(), future.cause());
                    }
                });
                
            } catch (Exception e) {
                errorResponses.incrementAndGet();
                log.error("[NettyServerConnection] 发送响应异常: " + connectionId, e);
                throw new RuntimeException("发送响应失败", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> sendError(Throwable error) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接不可用: " + connectionId));
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                // 更新活跃时间和错误计数
                lastActiveTime.set(System.currentTimeMillis());
                errorResponses.incrementAndGet();
                
                // 创建错误响应
                String errorMessage = String.format(
                    "{\"error\":{\"code\":500,\"message\":\"%s\",\"timestamp\":%d}}", 
                    error.getMessage(), System.currentTimeMillis()
                );
                
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    ctx.alloc().buffer().writeBytes(errorMessage.getBytes())
                );
                
                httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
                httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, errorMessage.length());
                
                // 发送错误响应
                ctx.writeAndFlush(httpResponse).addListener(future -> {
                    if (future.isSuccess()) {
                        log.debug("[NettyServerConnection] 错误响应发送成功: {}", getClientAddress());
                    } else {
                        log.error("[NettyServerConnection] 错误响应发送失败: {}", getClientAddress(), future.cause());
                    }
                });
                
            } catch (Exception e) {
                log.error("[NettyServerConnection] 发送错误响应异常: " + connectionId, e);
                throw new RuntimeException("发送错误响应失败", e);
            }
        });
    }
    
    @Override
    public String getClientAddress() {
        if (ctx != null && ctx.channel() != null && ctx.channel().remoteAddress() != null) {
            return ctx.channel().remoteAddress().toString();
        }
        return "unknown";
    }
    
    @Override
    public long getTotalResponses() {
        return totalResponses.get();
    }
    
    @Override
    public long getErrorResponses() {
        return errorResponses.get();
    }
    
    // 辅助方法
    
    /**
     * 转换Message为HTTP响应
     */
    private FullHttpResponse convertToHttpResponse(Message message) {
        // 创建HTTP响应
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK
        );
        
        // 设置响应内容
        if (message != null && message.getBody() != null) {
            byte[] content = message.getBody().getBytes();
            httpResponse.content().writeBytes(content);
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        } else {
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        }
        
        // 设置响应头
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        if (message != null && message.getHeaders() != null) {
            message.getHeaders().asMap().forEach((name, value) -> {
                if (value != null) {
                    httpResponse.headers().set(name, value.toString());
                }
            });
        }
        
        return httpResponse;
    }
    
    /**
     * 生成连接ID
     */
    private String generateConnectionId() {
        return "server-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 获取Netty上下文（供其他组件使用）
     */
    public ChannelHandlerContext getNettyContext() {
        return ctx;
    }
}