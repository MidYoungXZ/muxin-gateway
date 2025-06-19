package com.muxin.gateway.core.route.filter;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/22 15:40
 */
public interface GlobalFilter extends RouteFilter {

    default FilterTypeEnum filterType() {
        return FilterTypeEnum.GLOBAL;
    }

}
