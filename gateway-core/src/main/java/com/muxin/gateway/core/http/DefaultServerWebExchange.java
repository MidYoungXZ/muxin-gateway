package com.muxin.gateway.core.http;

import com.muxin.gateway.core.common.ProcessingPhase;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 15:54
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultServerWebExchange implements ServerWebExchange{

    private HttpServerRequest request;

    private HttpServerResponse response;

    private ChannelHandlerContext ctx;

    private ProcessingPhase phase;

    @Builder.Default
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public HttpServerRequest getRequest() {
        return request;
    }

    @Override
    public HttpServerResponse getResponse() {
        return response;
    }

    @Override
    public void setResponse(HttpServerResponse response) {
        this.response = response;
    }

    @Override
    public ChannelHandlerContext inboundContext() {
        return ctx;
    }

    @Override
    public ProcessingPhase processingPhase() {
        return phase;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static DefaultServerWebExchangeBuilder builder() {
        return new DefaultServerWebExchangeBuilder();
    }

    public static class DefaultServerWebExchangeBuilder {
        private HttpServerRequest request;
        private HttpServerResponse response;
        private ChannelHandlerContext ctx;
        private ProcessingPhase phase;
        private Map<String, Object> attributes = new ConcurrentHashMap<>();

        public DefaultServerWebExchangeBuilder request(HttpServerRequest request) {
            this.request = request;
            return this;
        }

        public DefaultServerWebExchangeBuilder response(HttpServerResponse response) {
            this.response = response;
            return this;
        }

        public DefaultServerWebExchangeBuilder ctx(ChannelHandlerContext ctx) {
            this.ctx = ctx;
            return this;
        }

        public DefaultServerWebExchangeBuilder phase(ProcessingPhase phase) {
            this.phase = phase;
            return this;
        }

        public DefaultServerWebExchangeBuilder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public DefaultServerWebExchange build() {
            DefaultServerWebExchange exchange = new DefaultServerWebExchange();
            exchange.request = this.request;
            exchange.response = this.response;
            exchange.ctx = this.ctx;
            exchange.phase = this.phase;
            exchange.attributes = this.attributes;
            return exchange;
        }
    }
}
