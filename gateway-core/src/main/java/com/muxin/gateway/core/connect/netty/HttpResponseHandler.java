package com.muxin.gateway.core.connect.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private static final Map<Channel, CompletableFuture<FullHttpResponse>> RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public static void registerFuture(Channel channel, CompletableFuture<FullHttpResponse> future) {
        RESPONSE_FUTURES.put(channel, future);
    }

    public static void removeFuture(Channel channel) {
        RESPONSE_FUTURES.remove(channel);
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
