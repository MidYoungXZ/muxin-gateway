package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.utils.ResponseUtil;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 15:47
 */
public class MockEndpointFilter implements GlobalFilter {


    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        String body = "{\"code\": \"000000\", \"message\": \"success\", \"data\": { \"mockData\": \"mockData\" }}";
        exchange.setOriginalResponse(ResponseUtil.createResponse(HttpResponseStatus.OK, body));
    }
}
