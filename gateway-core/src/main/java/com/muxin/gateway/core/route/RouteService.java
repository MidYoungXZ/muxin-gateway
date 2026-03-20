package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.service.EndpointAddress;

import java.util.List;
import java.util.Map;

/**
 * 路由服务接口
 * 简化版本：移除协议抽象
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface RouteService {

    ServiceDefinition serviceDefinition();

    List<EndpointAddress> getTargetAddresses();

    Map<String, Object> getTargetConfig();

    EndpointAddress selectTarget(RequestContext context, LoadBalanceStrategy loadBalanceStrategy);

    void refresh();

    boolean isHealthy();

    String getServiceId();
}
