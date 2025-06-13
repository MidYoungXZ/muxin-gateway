package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.common.GatewayConstants;
import com.muxin.gateway.core.common.ProcessingPhase;
import com.muxin.gateway.core.http.*;
import com.muxin.gateway.core.utils.RemotingUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Exchange处理适配器
 */
@Slf4j
public class ExchangeHandlerAdapter extends ChannelInboundHandlerAdapter implements ExchangeHandler {

    private final ExchangeHandler delegate;

    public ExchangeHandlerAdapter(ExchangeHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            super.channelRead(ctx, msg);
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        ServerWebExchange webExchange = null;
        
        try {
            // 构建HTTP请求包装
            HttpServerRequest serverRequest = buildServerRequest(request, ctx);
            
            // 创建交换对象
            webExchange = DefaultServerWebExchange.builder()
                    .request(serverRequest)
                    .ctx(ctx)
                    .phase(new ProcessingPhase().running())
                    .build();
            
            // 设置请求属性
            webExchange.setAttribute(GatewayConstants.GATEWAY_REQUEST_START_TIME_ATTR, System.currentTimeMillis());
            webExchange.setAttribute(GatewayConstants.GATEWAY_REQUEST_ID_ATTR, UUID.randomUUID().toString());
            
            // 处理请求
            handle(webExchange);
            
        } catch (Exception e) {
            log.error("Error processing request", e);
            // 确保资源被释放
            ReferenceCountUtil.release(request);
            if (webExchange != null) {
                sendErrorResponse(webExchange, e);
            }
        }
    }

    @Override
    public void handle(ServerWebExchange exchange) {
        if (delegate != null) {
            delegate.handle(exchange);
        } else {
            // 默认处理逻辑
            log.warn("No delegate handler configured, using default response");
            HttpServerResponse response = DefaultHttpServerResponse.json("{\"message\":\"Muxin Gateway is running\",\"status\":\"ok\"}");
            exchange.setResponse(response);
            
            // 写回响应
            exchange.inboundContext()
                    .writeAndFlush(response.getNettyResponse())
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            log.error("Failed to write response", future.cause());
                        }
                    });
        }
    }

    /**
     * 构建服务器请求对象
     */
    private HttpServerRequest buildServerRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        String remoteAddress = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        String host = request.headers().get("Host");
        
        return DefaultHttpServerRequest.builder()
                .beginTime(System.currentTimeMillis())
                .request(request)
                .uri(request.uri())
                .remoteAddress(remoteAddress)
                .host(host)
                .requestId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ServerWebExchange exchange, Exception e) {
        try {
            HttpServerResponse errorResponse = DefaultHttpServerResponse.of(io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}");
            
            exchange.inboundContext()
                    .writeAndFlush(errorResponse.getNettyResponse())
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            log.error("Failed to write error response", future.cause());
                        }
                    });
        } catch (Exception ex) {
            log.error("Failed to send error response", ex);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught in ExchangeHandlerAdapter", cause);
        ctx.close();
    }
}
