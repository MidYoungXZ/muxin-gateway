package com.muxin.gateway.core.filter;

/**
 * 定义了一个响应过滤器接口，继承自 {@link RouteRuleFilter}。
 * 该接口用于处理与响应相关的过滤逻辑。
 *
 * @author Administrator
 * @date 2024/11/19 16:40
 */
public interface ResponseFilter extends RouteRuleFilter {

    /**
     * 返回过滤器的类型，固定为 {@link FilterTypeEnum#RESPONSE}。
     *
     * @return 过滤器类型 {@link FilterTypeEnum#RESPONSE}
     */
    default FilterTypeEnum filterType() {
        return FilterTypeEnum.RESPONSE;
    }
}
