package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * EndpointFilter 404实现
 *
 * @author Administrator
 * @date 2024/11/21 14:44
 */
public class Filter404 implements EndpointFilter {


    private static final Filter404 instance;

    static {
        instance = new Filter404();
    }


    public static Filter404 instance(){
        return instance;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
//        HttpServerResponse response = ResponseUtil.createResponse(ResponseStatusEnum.G00_04_0004, ResponseStatusEnum.G00_04_0004.httpStatus().reasonPhrase());
//        exchange.setResponse(response);
    }

    @Override
    public int getOrder() {
        return 0;
    }


}
