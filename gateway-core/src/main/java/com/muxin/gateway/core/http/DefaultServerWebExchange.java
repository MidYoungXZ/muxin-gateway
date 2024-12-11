package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.ProcessingPhase;
import io.netty.channel.ChannelHandlerContext;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 15:54
 */
@Data
@Builder
public class DefaultServerWebExchange implements ServerWebExchange{

    private HttpServerRequest request;

    private HttpServerResponse response;

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
    public void setResponse(HttpServerResponse response) {

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
