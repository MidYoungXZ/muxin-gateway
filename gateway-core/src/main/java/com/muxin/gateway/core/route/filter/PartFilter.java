package com.muxin.gateway.core.route.filter;

/**
 * @projectname: muxin-gateway
 * @filename: Filter
 * @author: yangxz
 * @data:2025/6/14 21:43
 * @description:
 */
public interface PartFilter extends RouteFilter {

    default FilterTypeEnum filterType() {
        return FilterTypeEnum.GLOBAL;
    }
}
