package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.ServiceInstance;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.muxin.gateway.core.common.GatewayConstants.SERVICE_ID;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/10 15:14
 */
public class RoundRobinLoadBalancer implements GatewayLoadBalance {

    final AtomicInteger position = new AtomicInteger(0);

    RegisterCenter registerCenter;

    public RoundRobinLoadBalancer(RegisterCenter registerCenter) {
        this.registerCenter = registerCenter;
    }

    @Override
    public LbResponse<ServiceInstance> choose(LbRequest<ServerWebExchange> request) {
        String serviceId = request.getContext().getAttribute(SERVICE_ID);
        List<ServiceInstance> selectInstances = registerCenter.selectInstances(serviceId);
        if (ObjectUtils.isEmpty(selectInstances)) {
            return null;
        }
        int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;
        ServiceInstance instance = selectInstances.get(pos % selectInstances.size());
        return new DefaultLbResponse(instance);
    }

    @Override
    public String loadBalanceType() {
        return "Round";
    }
}
