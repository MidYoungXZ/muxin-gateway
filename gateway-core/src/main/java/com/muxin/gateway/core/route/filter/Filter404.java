package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.utils.ResponseUtil;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 404过滤器
 * 
 * 实现PartFilter接口，当请求无法匹配路由时返回404响应
 *
 * @author Administrator
 * @since 1.0.0
 */
public class Filter404 implements PartFilter {


    private static final Filter404 instance;

    static {
        instance = new Filter404();
    }


    public static Filter404 instance() {
        return instance;
    }

    @Override
    public void filter(ServerWebExchange exchange) {
        exchange.setOriginalResponse(ResponseUtil.createResponse(HttpResponseStatus.NOT_FOUND));
    }


    @Override
    public int getOrder() {
        return 10;
    }
}
