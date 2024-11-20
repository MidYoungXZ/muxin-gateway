package com.muxin.gateway.core.http;

import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 15:54
 */
public class DefaultServerWebExchange implements ServerWebExchange{


    @Override
    public HttpServerRequest getRequest() {
        return null;
    }

    @Override
    public HttpServerResponse getResponse() {
        return null;
    }

    @Override
    public Builder mutate() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }
}
