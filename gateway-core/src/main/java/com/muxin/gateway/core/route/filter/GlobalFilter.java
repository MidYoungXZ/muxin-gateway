package com.muxin.gateway.core.route.filter;

/**
 * 全局过滤器接口
 * 
 * 扩展RouteFilter接口，定义全局过滤器的默认类型
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface GlobalFilter extends RouteFilter {

    default FilterTypeEnum filterType() {
        return FilterTypeEnum.GLOBAL;
    }

}
