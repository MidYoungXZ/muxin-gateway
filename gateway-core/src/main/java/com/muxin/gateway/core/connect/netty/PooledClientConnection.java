package com.muxin.gateway.core.connect.netty;

import com.muxin.gateway.core.connect.ClientConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 池化的HTTP客户端连接实现
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class PooledClientConnection implements ClientConnection {

    private final Channel channel;
    private final NettyConnectionPool pool;
    private final String connectionId;
    private final long createdTime;

    private final AtomicBoolean inUse = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicLong lastActiveTime = new AtomicLong(System.currentTimeMillis());

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    public PooledClientConnection(Channel channel, NettyConnectionPool pool) {
        this.channel = channel;
        this.pool = pool;
        this.connectionId = channel.id().asShortText();
        this.createdTime = System.currentTimeMillis();
        this.inUse.set(true);
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public boolean isActive() {
        return !closed.get() && channel != null && channel.isActive();
    }

    @Override
    public CompletableFuture<Void> close() {
        if (closed.compareAndSet(false, true)) {
            pool.returnChannel(channel);
            log.debug("连接关闭: {}", connectionId);
        }
        return CompletableFuture.completedFuture(null);
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
    public boolean isHealthy() {
        return isActive() && channel.isWritable();
    }

    @Override
    public CompletableFuture<FullHttpResponse> send(FullHttpRequest request) {
        totalRequests.incrementAndGet();
        lastActiveTime.set(System.currentTimeMillis());

        CompletableFuture<FullHttpResponse> future = new CompletableFuture<>();

        if (!isActive()) {
            totalFailures.incrementAndGet();
            future.completeExceptionally(new RuntimeException("连接不活跃: " + connectionId));
            return future;
        }

        try {
            HttpResponseHandler responseHandler = channel.pipeline().get(HttpResponseHandler.class);
            if (responseHandler == null) {
                totalFailures.incrementAndGet();
                future.completeExceptionally(new RuntimeException("响应处理器未配置"));
                return future;
            }

            HttpResponseHandler.registerFuture(channel, future);

            FullHttpRequest requestToSend = duplicateRequest(request);

            log.debug("[PooledClientConnection] >>> 发送请求到后端");
            log.debug("[PooledClientConnection] >>> URL: {} {} {}", requestToSend.method(), requestToSend.uri(), requestToSend.protocolVersion());
            log.debug("[PooledClientConnection] >>> Headers: {}", requestToSend.headers());
            if (requestToSend.content().isReadable()) {
                String body = requestToSend.content().toString(java.nio.charset.StandardCharsets.UTF_8);
                log.debug("[PooledClientConnection] >>> Body: {}", body);
            } else {
                log.debug("[PooledClientConnection] >>> Body: (empty)");
            }

            channel.writeAndFlush(requestToSend).addListener((GenericFutureListener<Future<Void>>) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.debug("[PooledClientConnection] 请求发送成功: {}", connectionId);
                } else {
                    totalFailures.incrementAndGet();
                    future.completeExceptionally(new RuntimeException("请求发送失败", channelFuture.cause()));
                }
            });
        } catch (Exception e) {
            totalFailures.incrementAndGet();
            future.completeExceptionally(e);
        }

        return future;
    }

    private FullHttpRequest duplicateRequest(FullHttpRequest original) {
        ByteBuf content = original.content();
        ByteBuf duplicatedContent = content != null && content.isReadable() 
                ? Unpooled.copiedBuffer(content) 
                : Unpooled.buffer(0);

        FullHttpRequest duplicated = new io.netty.handler.codec.http.DefaultFullHttpRequest(
                original.protocolVersion(),
                original.method(),
                original.uri(),
                duplicatedContent,
                original.headers().copy(),
                original.trailingHeaders().copy()
        );

        duplicated.headers().set(HttpHeaderNames.CONTENT_LENGTH, duplicatedContent.readableBytes());
        
        return duplicated;
    }

    @Override
    public void markInUse() {
        inUse.set(true);
        lastActiveTime.set(System.currentTimeMillis());
    }

    @Override
    public void markIdle() {
        inUse.set(false);
        lastActiveTime.set(System.currentTimeMillis());
    }

    @Override
    public boolean isInUse() {
        return inUse.get();
    }

    @Override
    public void returnToPool() {
        if (isActive()) {
            markIdle();
            pool.returnChannel(channel);
        }
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    public long getTotalRequests() {
        return totalRequests.get();
    }

    @Override
    public long getTotalFailures() {
        return totalFailures.get();
    }

    Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "PooledClientConnection{" +
                "connectionId='" + connectionId + '\'' +
                ", active=" + isActive() +
                ", inUse=" + inUse.get() +
                ", channel=" + channel +
                '}';
    }
}
