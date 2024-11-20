package com.muxin.gateway.core.filter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 15:21
 */
public interface EndpointFilter extends GatewayFilter{

    default FilterTypeEnum filterType(){
        return FilterTypeEnum.ENDPOINT;
    }


    
}
