package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.common.ProcessingPhase;
import com.muxin.gateway.core.http.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 11:14
 */
public class ExchangeHandlerAdapter extends ChannelInboundHandlerAdapter implements ExchangeHandler {

    private final ExchangeHandler delegate;


    public ExchangeHandlerAdapter(ExchangeHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //todo   FullHttpRequest转 HttpServerRequest  ServerWebExchange Disruptor实现对象池  JCTools
        FullHttpRequest request = (FullHttpRequest) msg;
        HttpServerRequest serverRequest = DefaultHttpServerRequest.builder()
                .beginTime(System.currentTimeMillis())
                .request(request)
                .build();
        ServerWebExchange webExchange = DefaultServerWebExchange.builder()
                .request(serverRequest)
                .ctx(ctx)
                .phase(new ProcessingPhase().running())
                .build();
        //webHandle
        handle(webExchange);
    }


    @Override
    public void handle(ServerWebExchange exchange) {
        delegate.handle(exchange);
    }


}
