package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.registry.ServiceInstance;

/**
 * 默认负载均衡响应类
 * 
 * 实现LbResponse接口，提供默认的负载均衡响应实现
 *
 * @author Administrator
 * @since 1.0.0
 */
public class DefaultLbResponse implements LbResponse<ServiceInstance> {

    private final ServiceInstance instance;

    public DefaultLbResponse(ServiceInstance instance) {
        this.instance = instance;
    }

    @Override
    public boolean hasServer() {
        return null != instance;
    }

    @Override
    public ServiceInstance getServer() {
        return instance;
    }
}
