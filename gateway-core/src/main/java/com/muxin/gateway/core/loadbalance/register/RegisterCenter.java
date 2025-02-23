package com.muxin.gateway.core.loadbalance.register;

import java.util.List;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/10 14:31
 */
public interface RegisterCenter {

    void register(ServiceInstance instance);

    void deregister(ServiceInstance instance);

    List<ServiceInstance> selectInstances(String serviceId);

    List<ServiceInstance> selectInstances(String serviceId, Boolean healthy);

    void subscribe(String serviceId, RegisterCenterListener listener);

}
