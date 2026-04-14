package com.muxin.gateway.core.connect.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private static final Map<Channel, CompletableFuture<FullHttpResponse>> RESPONSE_FUTURES = new ConcurrentHashMap<>();

    // Future超时时间（默认5分钟）
    private static final long FUTURE_TIMEOUT_MS = 5 * 60 * 1000L;
    // 清理间隔（默认1分钟）
    private static final long CLEANUP_INTERVAL_MS = 60 * 1000L;

    // 使用单线程调度器进行定期清理
    private static final ScheduledExecutorService CLEANUP_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "response-future-cleanup");
        t.setDaemon(true);
        return t;
    });

    // 记录Future创建时间的辅助Map
    private static final Map<Channel, AtomicLong> FUTURE_CREATE_TIMES = new ConcurrentHashMap<>();

    // 静态初始化块：启动清理任务
    static {
        CLEANUP_SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredFutures();
            } catch (Exception e) {
                log.warn("[HttpResponseHandler] 清理任务异常: {}", e.getMessage());
            }
        }, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 清理超时的Future，防止内存泄漏
     */
    private static void cleanupExpiredFutures() {
        long now = System.currentTimeMillis();
        int cleanedCount = 0;

        Iterator<Map.Entry<Channel, CompletableFuture<FullHttpResponse>>> iterator =
                RESPONSE_FUTURES.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Channel, CompletableFuture<FullHttpResponse>> entry = iterator.next();
            Channel channel = entry.getKey();
            CompletableFuture<FullHttpResponse> future = entry.getValue();

            // 检查Future是否已完成或通道已关闭
            if (future.isDone() || !channel.isActive()) {
                iterator.remove();
                FUTURE_CREATE_TIMES.remove(channel);
                cleanedCount++;
                continue;
            }

            // 检查是否超时
            AtomicLong createTime = FUTURE_CREATE_TIMES.get(channel);
            if (createTime != null && (now - createTime.get()) > FUTURE_TIMEOUT_MS) {
                iterator.remove();
                FUTURE_CREATE_TIMES.remove(channel);
                future.completeExceptionally(new RuntimeException("Future等待超时，已自动清理"));
                cleanedCount++;
                log.warn("[HttpResponseHandler] 清理超时Future: channel={}", channel.id().asShortText());
            }
        }

        if (cleanedCount > 0) {
            log.debug("[HttpResponseHandler] 清理了 {} 个过期Future，当前剩余 {}", cleanedCount, RESPONSE_FUTURES.size());
        }
    }

    public static void registerFuture(Channel channel, CompletableFuture<FullHttpResponse> future) {
        RESPONSE_FUTURES.put(channel, future);
        FUTURE_CREATE_TIMES.put(channel, new AtomicLong(System.currentTimeMillis()));
    }

    public static void removeFuture(Channel channel) {
        RESPONSE_FUTURES.remove(channel);
        FUTURE_CREATE_TIMES.remove(channel);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
        CompletableFuture<FullHttpResponse> future = RESPONSE_FUTURES.remove(ctx.channel());

        if (future != null) {
            log.debug("[HttpResponseHandler] <<< 收到后端响应");
            log.debug("[HttpResponseHandler] <<< Status: {}", response.status());
            log.debug("[HttpResponseHandler] <<< Headers: {}", response.headers());
            if (response.content().isReadable()) {
                String body = response.content().toString(java.nio.charset.StandardCharsets.UTF_8);
                log.debug("[HttpResponseHandler] <<< Body: {}", body);
            } else {
                log.debug("[HttpResponseHandler] <<< Body: (empty)");
            }
            future.complete(response);
        } else {
            log.warn("[HttpResponseHandler] 未找到对应的Future: channel={}", ctx.channel().id().asShortText());
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        CompletableFuture<FullHttpResponse> future = RESPONSE_FUTURES.remove(ctx.channel());
        if (future != null && !future.isDone()) {
            log.warn("[HttpResponseHandler] 连接关闭: channel={}", ctx.channel().id().asShortText());
            future.completeExceptionally(new RuntimeException("连接已关闭: " + ctx.channel().id().asShortText()));
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        CompletableFuture<FullHttpResponse> future = RESPONSE_FUTURES.remove(ctx.channel());
        if (future != null && !future.isDone()) {
            log.error("[HttpResponseHandler] 连接异常: channel={}, cause={}", 
                    ctx.channel().id().asShortText(), cause);
            future.completeExceptionally(new RuntimeException("连接异常: " + cause.getMessage(), cause));
        }
        ctx.close();
    }
}
