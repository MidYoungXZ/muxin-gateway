package com.muxin.gateway.core.filter;

/**
 * 定义了一个端点过滤器接口，继承自 {@link RouteRuleFilter}。
 * 该接口用于处理与端点相关的过滤逻辑。
 *
 * @author Administrator
 * @date 2024/11/20 15:21
 */
public interface EndpointFilter extends RouteRuleFilter {

    /**
     * 返回过滤器的类型，固定为 {@link FilterTypeEnum#ENDPOINT}。
     *
     * @return 过滤器类型 {@link FilterTypeEnum#ENDPOINT}
     */
    default FilterTypeEnum filterType() {
        return FilterTypeEnum.ENDPOINT;
    }
}
