package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.loadbalance.register.ServiceInstance;

/**
 * 定义了一个网关负载均衡接口，继承自 {@link LoadBalance} 接口。
 * 该接口用于根据传入的 {@link ServerWebExchange} 对象选择合适的服务实例。
 *
 * @author Administrator
 * @date 2025/1/9 17:44
 */
public interface GatewayLoadBalance extends LoadBalance<ServiceInstance, ServerWebExchange> {

    /**
     * 返回负载均衡的类型。
     *
     * @return 负载均衡的类型
     */
    String loadBalanceType();
}
