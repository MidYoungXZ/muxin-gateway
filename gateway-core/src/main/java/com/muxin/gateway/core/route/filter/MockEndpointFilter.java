package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.utils.ResponseUtil;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 模拟端点过滤器
 * 
 * 实现GlobalFilter接口，返回模拟的响应数据
 *
 * @author Administrator
 * @since 1.0.0
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
