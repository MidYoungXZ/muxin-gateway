package com.muxin.gateway.core.http;

import com.muxin.gateway.core.filter.FilterChain;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 11:14
 */
public class DispatcherHandler extends ChannelInboundHandlerAdapter implements WebHandler {


    private FilterChain requestChain = null;

    private FilterChain endpointChain = null;

    private FilterChain responseChain = null;




    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        handle(null);
    }


    /**
     * 1.查找路由
     * 2.拼装filter
     * 3.执行filter
     * 4.处理返回
     * @param exchange
     */
    @Override
    public void handle(ServerWebExchange exchange) {

    }


}
