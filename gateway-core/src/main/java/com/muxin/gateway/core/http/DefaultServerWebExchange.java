package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.ProcessingPhase;
import io.netty.channel.ChannelHandlerContext;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 默认ServerWebExchange实现
 *
 * @author Administrator
 * @date 2024/11/20 15:54
 */
@Data
@Builder
public class DefaultServerWebExchange implements ServerWebExchange{

    private HttpServerOperations httpServerOperations;

    private ChannelHandlerContext ctx;

    private ProcessingPhase phase;


    @Override
    public HttpServerRequest getRequest() {
        return null;
    }

    @Override
    public HttpServerResponse getResponse() {
        return null;
    }


    @Override
    public ChannelHandlerContext inboundContext() {
        return null;
    }

    @Override
    public ProcessingPhase processingPhase() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }
}
