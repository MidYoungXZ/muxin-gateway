package com.muxin.gateway.core.filter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/19 16:41
 */
public interface RequestFilter extends GatewayFilter {

    default FilterTypeEnum filterType(){
        return FilterTypeEnum.REQUEST;
    }


}
