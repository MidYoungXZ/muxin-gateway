package com.muxin.gateway.core.filter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/19 16:40
 */

public interface ResponseFilter extends GatewayFilter {

    default FilterTypeEnum filterType() {
        return FilterTypeEnum.RESPONSE;
    }

}
