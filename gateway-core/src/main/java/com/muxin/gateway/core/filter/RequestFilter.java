package com.muxin.gateway.core.filter;

/**
 * 定义了一个请求过滤器接口，继承自 {@link RouteRuleFilter}。
 * 该接口用于处理与请求相关的过滤逻辑。
 *
 * @author Administrator
 * @date 2024/11/19 16:41
 */
public interface RequestFilter extends RouteRuleFilter {

    /**
     * 返回过滤器的类型，固定为 {@link FilterTypeEnum#REQUEST}。
     *
     * @return 过滤器类型 {@link FilterTypeEnum#REQUEST}
     */
    default FilterTypeEnum filterType() {
        return FilterTypeEnum.REQUEST;
    }
}
