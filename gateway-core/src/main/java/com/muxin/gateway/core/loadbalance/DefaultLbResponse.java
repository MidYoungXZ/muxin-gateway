package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.loadbalance.register.ServiceInstance;

/**
 * 默认的负载均衡响应类，实现了 {@link LbResponse} 接口。
 * 该类用于封装负载均衡选择的服务实例。
 *
 * @author Administrator
 * @date 2025/1/10 15:27
 */
public class DefaultLbResponse implements LbResponse<ServiceInstance> {

    private final ServiceInstance instance;

    /**
     * 构造函数，用于初始化负载均衡响应对象。
     *
     * @param instance 选择的服务实例
     */
    public DefaultLbResponse(ServiceInstance instance) {
        this.instance = instance;
    }

    /**
     * 检查是否包含服务实例。
     *
     * @return 如果包含服务实例，返回 true；否则返回 false
     */
    @Override
    public boolean hasServer() {
        return null != instance;
    }

    /**
     * 获取选择的服务实例。
     *
     * @return 选择的服务实例
     */
    @Override
    public ServiceInstance getServer() {
        return instance;
    }
}
