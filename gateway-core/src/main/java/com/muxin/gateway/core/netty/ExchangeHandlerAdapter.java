package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.common.ProcessingPhase;
import com.muxin.gateway.core.http.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 实现了一个Netty的通道入站处理器适配器，继承自 {@link ChannelInboundHandlerAdapter} 并实现了 {@link ExchangeHandler} 接口。
 * 该类负责将Netty的 `FullHttpRequest` 转换为 `HttpServerRequest` 和 `ServerWebExchange`，并调用委托的 `ExchangeHandler` 处理请求。
 *
 * @author Administrator
 * @date 2024/11/20 11:14
 */
public class ExchangeHandlerAdapter extends ChannelInboundHandlerAdapter implements ExchangeHandler {

    private final ExchangeHandler delegate;

    /**
     * 构造函数，用于初始化适配器并设置委托的 `ExchangeHandler`。
     *
     * @param delegate 委托的 `ExchangeHandler`
     */
    public ExchangeHandlerAdapter(ExchangeHandler delegate) {
        this.delegate = delegate;
    }

    /**
     * 处理接收到的Netty通道消息。
     *
     * @param ctx 通道上下文
     * @param msg 接收到的消息
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 将 FullHttpRequest 转换为 HttpServerRequest 和 ServerWebExchange
        FullHttpRequest request = (FullHttpRequest) msg;
        HttpServerOperations serverOperations = new HttpServerOperations(request, ctx);
        ServerWebExchange webExchange = DefaultServerWebExchange.builder()
                .httpServerOperations(serverOperations)
                .ctx(ctx)
                .phase(new ProcessingPhase().running())
                .build();
        // 调用委托的 ExchangeHandler 处理请求
        handle(webExchange);
    }

    /**
     * 处理 `ServerWebExchange` 请求。
     *
     * @param exchange 当前的 `ServerWebExchange` 对象
     */
    @Override
    public void handle(ServerWebExchange exchange) {
        delegate.handle(exchange);
    }
}
