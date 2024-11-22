package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.http.ExchangeHandler;
import com.muxin.gateway.core.http.ServerWebExchange;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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


        handle(null);
    }



    @Override
    public void handle(ServerWebExchange exchange) {
        delegate.handle(exchange);
    }


}
