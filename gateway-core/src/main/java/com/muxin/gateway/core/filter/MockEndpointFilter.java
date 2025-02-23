package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * 模拟返回
 *
 * @author Administrator
 * @date 2025/1/9 15:47
 */
public class MockEndpointFilter implements GlobalFilter{


    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        String body = "{\"code\": \"000000\", \"message\": \"success\", \"data\": { \"mockData\": \"mockData\" }}";
//        ResponseUtil.createResponse(ResponseStatusEnum.G00_00_0000,body);
    }

    @Override
    public FilterTypeEnum filterType() {
        return FilterTypeEnum.ENDPOINT;
    }


}
