package com.muxin.gateway.core.plus.connect;

import com.muxin.gateway.core.plus.protocol.message.Message;
import com.muxin.gateway.core.plus.protocol.message.Protocol;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMessage;
import com.muxin.gateway.core.plus.route.service.EndpointAddress;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于Netty的客户端连接实现
 * 封装Netty Channel，提供统一的客户端连接抽象
 * 
 * @author muxin
 */
@Slf4j
public class NettyClientConnection implements ClientConnection {

    private final String connectionId;
    private final EndpointAddress target;
    private final Protocol protocol;
    private final Channel channel;
    private final Map<String, Object> attributes;
    private final Map<String, Object> options;
    private final long createdTime;
    private final AtomicLong lastActiveTime;
    private final AtomicBoolean inUse;
    private final AtomicLong totalRequests;
    private final AtomicLong totalFailures;
    private volatile ConnectionPool connectionPool;

    public NettyClientConnection(EndpointAddress target, Protocol protocol, Channel channel, Map<String, Object> options) {
        this.connectionId = generateConnectionId();
        this.target = target;
        this.protocol = protocol;
        this.channel = channel;
        this.options = options != null ? new ConcurrentHashMap<>(options) : new ConcurrentHashMap<>();
        this.attributes = new ConcurrentHashMap<>();
        this.createdTime = System.currentTimeMillis();
        this.lastActiveTime = new AtomicLong(System.currentTimeMillis());
        this.inUse = new AtomicBoolean(false);
        this.totalRequests = new AtomicLong(0);
        this.totalFailures = new AtomicLong(0);
        
        // 设置响应处理器
        setupResponseHandler();
        
        log.debug("[NettyClientConnection] 创建客户端连接: {} -> {}", connectionId, target.toUri());
    }

    private void setupResponseHandler() {
        // 添加响应处理器到管道
        channel.pipeline().addLast("response-handler", new SimpleChannelInboundHandler<HttpObject>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                if (msg instanceof FullHttpResponse) {
                    FullHttpResponse response = (FullHttpResponse) msg;
                    // 这里可以处理响应，但由于是连接池模式，实际的响应处理会在具体的请求中完成
                    log.debug("[NettyClientConnection] 收到响应: {} - {}", connectionId, response.status());
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                log.error("[NettyClientConnection] 连接异常: {}", connectionId, cause);
                totalFailures.incrementAndGet();
            }
        });
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (channel != null && channel.isActive()) {
                    channel.close().sync();
                }
                log.debug("[NettyClientConnection] 客户端连接关闭: {}", connectionId);
            } catch (Exception e) {
                log.error("[NettyClientConnection] 关闭连接异常: {}", connectionId, e);
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

    @Override
    public boolean isHealthy() {
        try {
            // 基础健康检查
            if (!isActive()) {
                return false;
            }

            // 检查连接是否可写
            if (!channel.isWritable()) {
                log.debug("[NettyClientConnection] 连接不可写: {}", connectionId);
                return false;
            }

            // 检查失败率
            long requests = totalRequests.get();
            long failures = totalFailures.get();
            if (requests > 10) { // 至少有10个请求才计算失败率
                double failureRate = (double) failures / requests;
                if (failureRate > 0.2) { // 失败率超过20%认为不健康
                    log.debug("[NettyClientConnection] 连接失败率过高: {} - {}/{}", 
                        connectionId, failures, requests);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.debug("[NettyClientConnection] 健康检查异常: {}", connectionId, e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Message> send(Message request) {
        if (!isActive()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("连接已断开: " + connectionId)
            );
        }

        CompletableFuture<Message> future = new CompletableFuture<>();
        totalRequests.incrementAndGet();
        lastActiveTime.set(System.currentTimeMillis());

        try {
            // 将Message转换为HTTP请求
            FullHttpRequest httpRequest = convertToHttpRequest(request);
            
            // 创建一个临时的响应处理器
            String responseHandlerId = "response-" + UUID.randomUUID().toString();
            channel.pipeline().addLast(responseHandlerId, new SimpleChannelInboundHandler<FullHttpResponse>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
                    try {
                        // 转换响应为Message
                        Message responseMessage = convertToMessage(response, request);
                        future.complete(responseMessage);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    } finally {
                        // 移除临时处理器
                        channel.pipeline().remove(responseHandlerId);
                    }
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    totalFailures.incrementAndGet();
                    future.completeExceptionally(cause);
                    channel.pipeline().remove(responseHandlerId);
                }
            });

            // 发送请求
            ChannelFuture writeFuture = channel.writeAndFlush(httpRequest);
            writeFuture.addListener(writeFuture1 -> {
                if (!writeFuture1.isSuccess()) {
                    totalFailures.incrementAndGet();
                    channel.pipeline().remove(responseHandlerId);
                    future.completeExceptionally(
                        new RuntimeException("发送请求失败", writeFuture1.cause())
                    );
                }
            });

            // 设置超时
            long timeout = (Long) options.getOrDefault("requestTimeout", 30000L);
            CompletableFuture.delayedExecutor(timeout, TimeUnit.MILLISECONDS).execute(() -> {
                if (!future.isDone()) {
                    totalFailures.incrementAndGet();
                    try {
                        channel.pipeline().remove(responseHandlerId);
                    } catch (Exception ignored) {}
                    future.completeExceptionally(new RuntimeException("请求超时"));
                }
            });

        } catch (Exception e) {
            totalFailures.incrementAndGet();
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public void markInUse() {
        inUse.set(true);
        lastActiveTime.set(System.currentTimeMillis());
    }

    @Override
    public void markIdle() {
        inUse.set(false);
    }

    @Override
    public boolean isInUse() {
        return inUse.get();
    }

    @Override
    public void returnToPool() {
        markIdle();
        if (connectionPool != null) {
            connectionPool.returnConnection(this);
        }
    }

    @Override
    public void destroy() {
        try {
            close().get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[NettyClientConnection] 销毁连接异常: {}", connectionId, e);
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

    /**
     * 设置连接池引用
     */
    public void setConnectionPool(ConnectionPool pool) {
        this.connectionPool = pool;
    }

    /**
     * 获取目标地址
     */
    public EndpointAddress getTarget() {
        return target;
    }

    /**
     * 获取底层Channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * 将Message转换为HTTP请求
     */
    private FullHttpRequest convertToHttpRequest(Message message) {
        try {
            if (!(message instanceof HttpMessage)) {
                throw new IllegalArgumentException("不支持的消息类型: " + message.getClass().getName());
            }

            HttpMessage httpMessage = (HttpMessage) message;
            
            // 创建HTTP方法
            HttpMethod method = HttpMethod.valueOf(httpMessage.method().toUpperCase());
            
            // 创建URI
            String uri = httpMessage.url().getPath();
            if (httpMessage.url().getQuery() != null) {
                uri += "?" + httpMessage.url().getQuery();
            }

            // 创建请求体
            ByteBuf content = Unpooled.EMPTY_BUFFER;
            if (httpMessage.getBody() != null && !httpMessage.getBody().isEmpty()) {
                byte[] bodyData = httpMessage.getBody().getBytes();
                content = Unpooled.wrappedBuffer(bodyData);
            }

            // 创建HTTP请求
            FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, method, uri, content
            );

            // 设置头部
            HttpHeaders headers = request.headers();
            headers.set(HttpHeaderNames.HOST, target.getHost() + ":" + target.getPort());
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            headers.set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            // 复制自定义头部
            if (httpMessage.getHeaders() != null) {
                for (String headerName : httpMessage.getHeaders().getNames()) {
                    Object headerValue = httpMessage.getHeaders().get(headerName, Object.class);
                    if (headerValue != null) {
                        headers.set(headerName, headerValue.toString());
                    }
                }
            }

            return request;
        } catch (Exception e) {
            throw new RuntimeException("转换HTTP请求失败", e);
        }
    }

    /**
     * 将HTTP响应转换为Message
     */
    private Message convertToMessage(FullHttpResponse response, Message originalRequest) {
        try {
            // 这里应该创建响应消息，暂时返回原始请求的响应版本
            return originalRequest.createResponse();
        } catch (Exception e) {
            throw new RuntimeException("转换响应消息失败", e);
        }
    }

    /**
     * 生成连接ID
     */
    private String generateConnectionId() {
        return "client-" + UUID.randomUUID().toString().substring(0, 8);
    }
} 