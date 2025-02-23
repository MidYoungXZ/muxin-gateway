package com.muxin.gateway.core.loadbalance;

/**
 * 定义了一个负载均衡接口。
 * 该接口用于根据传入的负载均衡请求选择合适的服务实例，并返回负载均衡响应。
 *
 * @param <S> 服务实例的类型
 * @param <C> 上下文的类型
 * @author Administrator
 * @date 2025/1/9 17:21
 */
public interface LoadBalance<S, C> {

    /**
     * 根据负载均衡请求选择合适的服务实例，并返回负载均衡响应。
     *
     * @param request 负载均衡请求
     * @return 负载均衡响应
     */
    LbResponse<S> choose(LbRequest<C> request);
}
