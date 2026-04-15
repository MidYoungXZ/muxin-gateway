package com.muxin.gateway.core.connect.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private static final Map<Channel, CompletableFuture<FullHttpResponse>> RESPONSE_FUTURES = new ConcurrentHashMap<>();

    private static final int MAX_PENDING_SIZE = 10000;

    private static final long FUTURE_TIMEOUT_MS = 60 * 1000L;

    private static final long CLEANUP_INTERVAL_MS = 30 * 1000L;

    private static final ScheduledExecutorService CLEANUP_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "response-future-cleanup");
        t.setDaemon(true);
        return t;
    });

    private static final Map<Channel, AtomicLong> FUTURE_CREATE_TIMES = new ConcurrentHashMap<>();

    private static volatile boolean shutdownRequested = false;

    static {
        CLEANUP_SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredFutures();
            } catch (Exception e) {
                log.warn("[HttpResponseHandler] 清理任务异常: {}", e.getMessage());
            }
        }, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }, "http-response-handler-shutdown"));
    }

    public static void shutdown() {
        if (shutdownRequested) {
            return;
        }
        shutdownRequested = true;
        log.info("[HttpResponseHandler] 开始关闭，清理静态资源...");

        CLEANUP_SCHEDULER.shutdownNow();

        int cancelledCount = 0;
        for (Map.Entry<Channel, CompletableFuture<FullHttpResponse>> entry : RESPONSE_FUTURES.entrySet()) {
            CompletableFuture<FullHttpResponse> future = entry.getValue();
            if (!future.isDone()) {
                future.completeExceptionally(new RuntimeException("Gateway shutdown"));
                cancelledCount++;
            }
        }

        RESPONSE_FUTURES.clear();
        FUTURE_CREATE_TIMES.clear();

        log.info("[HttpResponseHandler] 关闭完成，取消了 {} 个未完成的Future", cancelledCount);
    }

    private static void cleanupExpiredFutures() {
        if (shutdownRequested) {
            return;
        }

        long now = System.currentTimeMillis();
        int cleanedCount = 0;
        int currentSize = RESPONSE_FUTURES.size();

        if (currentSize > MAX_PENDING_SIZE) {
            log.warn("[HttpResponseHandler] pending futures 超过限制: {} > {}", currentSize, MAX_PENDING_SIZE);
        }

        RESPONSE_FUTURES.entrySet().removeIf(entry -> {
            Channel channel = entry.getKey();
            CompletableFuture<FullHttpResponse> future = entry.getValue();

            if (!channel.isActive() || future.isDone()) {
                FUTURE_CREATE_TIMES.remove(channel);
                if (!future.isDone() && !channel.isActive()) {
                    future.completeExceptionally(new RuntimeException("Channel inactive"));
                }
                return true;
            }

            AtomicLong createTime = FUTURE_CREATE_TIMES.get(channel);
            if (createTime != null && (now - createTime.get()) > FUTURE_TIMEOUT_MS) {
                FUTURE_CREATE_TIMES.remove(channel);
                future.completeExceptionally(new TimeoutException("Future等待超时，已自动清理"));
                log.warn("[HttpResponseHandler] 清理超时Future: channel={}", channel.id().asShortText());
                return true;
            }

            return false;
        });

        cleanedCount = currentSize - RESPONSE_FUTURES.size();

        if (cleanedCount > 0) {
            log.debug("[HttpResponseHandler] 清理了 {} 个过期Future，当前剩余 {}", cleanedCount, RESPONSE_FUTURES.size());
        }
    }

    public static void registerFuture(Channel channel, CompletableFuture<FullHttpResponse> future) {
        if (shutdownRequested) {
            future.completeExceptionally(new RuntimeException("Gateway shutdown"));
            return;
        }

        if (RESPONSE_FUTURES.size() >= MAX_PENDING_SIZE) {
            log.warn("[HttpResponseHandler] pending futures 达到上限: {}", MAX_PENDING_SIZE);
        }

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
