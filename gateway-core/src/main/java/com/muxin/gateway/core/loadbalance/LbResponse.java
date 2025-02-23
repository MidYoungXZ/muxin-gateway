package com.muxin.gateway.core.loadbalance;

/**
 * 定义了一个负载均衡响应接口。
 * 该接口用于表示负载均衡响应，并提供检查和服务实例获取的方法。
 *
 * @author Administrator
 * @date 2025/1/9 17:25
 */
public interface LbResponse<T> {

    /**
     * 检查是否包含服务实例。
     *
     * @return 如果包含服务实例，返回 true；否则返回 false
     */
    boolean hasServer();

    /**
     * 获取选择的服务实例。
     *
     * @return 选择的服务实例
     */
    T getServer();
}
