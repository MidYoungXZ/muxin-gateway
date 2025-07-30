package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.common.GatewayConstants;
import com.muxin.gateway.core.http.DefaultServerWebExchange;
import com.muxin.gateway.core.http.ExchangeHandler;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.utils.ExchangeUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
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
            // 直接从Netty请求创建交换对象
            webExchange = DefaultServerWebExchange.fromNettyRequest(request, ctx);

            // 设置请求属性
            webExchange.setAttribute(GatewayConstants.GATEWAY_REQUEST_START_TIME_ATTR, System.currentTimeMillis());
            webExchange.setAttribute(GatewayConstants.GATEWAY_REQUEST_ID_ATTR, UUID.randomUUID().toString());

            // 处理请求
            handle(webExchange);
        } catch (Exception e) {
            log.error("Error processing request", e);
            // 发送错误响应
            if (webExchange != null) {
                sendErrorResponse(webExchange, e);
            }
        } finally {
            ExchangeUtil.writeAndFlush(webExchange);
        }

    }

    @Override
    public void handle(ServerWebExchange exchange) {
        if (delegate != null) {
            delegate.handle(exchange);
        } else {
            sendErrorResponse(exchange, new RuntimeException("ExchangeHandler not found"));
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ServerWebExchange exchange, Exception e) {
        try {
            exchange.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Internal Server Error\",\"message\":\"" + e.getMessage() + "\"}");

            exchange.inboundContext().writeAndFlush(exchange.getOriginalResponse()).addListener(future -> {
                if (!future.isSuccess()) {
                    log.error("Failed to write error response", future.cause());
                }
                // 在响应写入完成后释放资源
                try {
                    exchange.release();
                } catch (Exception releaseEx) {
                    log.warn("Failed to release exchange resources", releaseEx);
                }
            });
        } catch (Exception ex) {
            log.error("Failed to send error response", ex);
            // 如果发送响应失败，也要释放资源
            try {
                exchange.release();
            } catch (Exception releaseEx) {
                log.warn("Failed to release exchange resources after error", releaseEx);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught in ExchangeHandlerAdapter", cause);
        ctx.close();
    }
}
