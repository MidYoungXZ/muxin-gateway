package com.muxin.gateway.core.plus.connect.netty;

import com.muxin.gateway.core.plus.connect.ServerConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class NettyServerConnection implements ServerConnection {

    private final Channel channel;
    private final String connectionId;
    private final long createdTime;
    private final AtomicLong lastActiveTime;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    public NettyServerConnection(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel不能为空");
        }
        this.channel = channel;
        this.connectionId = channel.id().asShortText();
        this.createdTime = System.currentTimeMillis();
        this.lastActiveTime = new AtomicLong(System.currentTimeMillis());
        log.debug("[NettyServerConnection] 创建服务端连接: {}, 客户端地址: {}", 
                connectionId, getClientAddress());
    }

    @Override
    public CompletableFuture<Void> sendResponse(FullHttpResponse response) {
        totalRequests.incrementAndGet();
        lastActiveTime.set(System.currentTimeMillis());

        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!isActive()) {
            totalFailures.incrementAndGet();
            future.completeExceptionally(new RuntimeException("连接已关闭: " + connectionId));
            return future;
        }

        try {
            boolean keepAlive = HttpUtil.isKeepAlive(response);

            FullHttpResponse responseToSend = duplicateResponse(response);

            channel.writeAndFlush(responseToSend).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.debug("[NettyServerConnection] 响应发送成功: {}, keepAlive={}", connectionId, keepAlive);
                    future.complete(null);

                    if (!keepAlive) {
                        channel.close();
                    }
                } else {
                    totalFailures.incrementAndGet();
                    log.error("[NettyServerConnection] 响应发送失败: {}", connectionId, channelFuture.cause());
                    future.completeExceptionally(new RuntimeException("响应发送失败", channelFuture.cause()));
                }
            });
        } catch (Exception e) {
            totalFailures.incrementAndGet();
            log.error("[NettyServerConnection] 发送响应异常: {}", connectionId, e);
            future.completeExceptionally(e);
        }

        return future;
    }

    private FullHttpResponse duplicateResponse(FullHttpResponse original) {
        ByteBuf content = original.content();
        ByteBuf duplicatedContent = content != null && content.isReadable()
                ? Unpooled.copiedBuffer(content)
                : Unpooled.buffer(0);

        FullHttpResponse duplicated = new DefaultFullHttpResponse(
                original.protocolVersion(),
                original.status(),
                duplicatedContent,
                original.headers().copy(),
                original.trailingHeaders().copy()
        );

        duplicated.headers().set(HttpHeaderNames.CONTENT_LENGTH, duplicatedContent.readableBytes());

        return duplicated;
    }

    @Override
    public CompletableFuture<Void> sendError(Throwable error) {
        totalRequests.incrementAndGet();
        lastActiveTime.set(System.currentTimeMillis());

        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!isActive()) {
            totalFailures.incrementAndGet();
            future.completeExceptionally(new RuntimeException("连接已关闭: " + connectionId));
            return future;
        }

        try {
            int statusCode = determineStatusCode(error);
            String errorMessage = buildErrorMessage(error);

            ByteBuf content = Unpooled.copiedBuffer(errorMessage, StandardCharsets.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(statusCode),
                    content
            );

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            channel.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.debug("[NettyServerConnection] 错误响应发送成功: {}, status={}", connectionId, statusCode);
                    future.complete(null);
                } else {
                    totalFailures.incrementAndGet();
                    log.error("[NettyServerConnection] 错误响应发送失败: {}", connectionId, channelFuture.cause());
                    future.completeExceptionally(new RuntimeException("错误响应发送失败", channelFuture.cause()));
                }
                channel.close();
            });

        } catch (Exception e) {
            totalFailures.incrementAndGet();
            log.error("[NettyServerConnection] 发送错误响应异常: {}", connectionId, e);
            future.completeExceptionally(e);
            channel.close();
        }

        return future;
    }

    private int determineStatusCode(Throwable error) {
        if (error instanceof com.muxin.gateway.core.plus.GatewayProcessor.ProcessingException) {
            return 500;
        }
        if (error instanceof IllegalArgumentException) {
            return 400;
        }
        if (error instanceof RuntimeException) {
            String message = error.getMessage();
            if (message != null) {
                if (message.contains("路由匹配失败") || message.contains("路由未找到")) {
                    return 404;
                }
                if (message.contains("端点选择失败") || message.contains("没有可用")) {
                    return 503;
                }
                if (message.contains("连接获取失败") || message.contains("连接不活跃")) {
                    return 502;
                }
                if (message.contains("过滤器执行失败")) {
                    return 500;
                }
                if (message.contains("超时") || message.contains("timeout")) {
                    return 504;
                }
            }
        }
        return 500;
    }

    private String buildErrorMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null) {
            message = error.getClass().getSimpleName();
        }
        return String.format(
                "{\"error\":{\"code\":%d,\"message\":\"%s\",\"timestamp\":%d}}",
                determineStatusCode(error),
                escapeJson(message),
                System.currentTimeMillis()
        );
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String getClientAddress() {
        return channel.remoteAddress().toString();
    }

    @Override
    public String getRemoteHost() {
        if (channel.remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
            return addr.getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getRemotePort() {
        if (channel.remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
            return addr.getPort();
        }
        return 0;
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
            if (channel != null && channel.isActive()) {
                channel.close();
            }
            log.debug("[NettyServerConnection] 连接关闭: {}", connectionId);
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

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getTotalFailures() {
        return totalFailures.get();
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "NettyServerConnection{" +
                "connectionId='" + connectionId + '\'' +
                ", clientAddress='" + getClientAddress() + '\'' +
                ", active=" + isActive() +
                ", totalRequests=" + totalRequests.get() +
                '}';
    }
}
