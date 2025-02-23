package com.muxin.gateway.core.loadbalance.register;

import java.util.List;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/10 14:52
 */
public interface RegisterCenterListener {

    void onChange(List<ServiceInstance> instances);

}
