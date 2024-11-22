package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.common.ResponseStatusEnum;
import com.muxin.gateway.core.http.HttpServerResponse;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.utils.ResponseUtil;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 14:44
 */
public class Filter404 implements EndpointFilter {

    @Override
    public void filter(ServerWebExchange exchange) {
        HttpServerResponse response = ResponseUtil.createResponse(ResponseStatusEnum.G00_04_0004, ResponseStatusEnum.G00_04_0004.httpStatus().reasonPhrase());
        exchange.setResponse(response);
    }

    @Override
    public int getOrder() {
        return 0;
    }


}
