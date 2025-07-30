package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.registry.ServiceInstance;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/10 15:27
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
