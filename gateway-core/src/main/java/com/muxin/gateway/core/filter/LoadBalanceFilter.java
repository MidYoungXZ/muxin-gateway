package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 16:45
 */
public class LoadBalanceFilter implements GlobalFilter {


    @Override
    public void filter(ServerWebExchange exchange) {

    }

    @Override
    public FilterTypeEnum filterType() {
        return FilterTypeEnum.REQUEST;
    }




    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 100;
    }
}
