package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.ServiceInstance;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.muxin.gateway.core.common.GatewayConstants.SERVICE_ID;

/**
 * 随机负载均衡算法
 * 随机选择一个可用的服务实例
 */
public class RandomLoadBalancer implements GatewayLoadBalance {

    private final RegisterCenter registerCenter;

    public RandomLoadBalancer(RegisterCenter registerCenter) {
        this.registerCenter = registerCenter;
    }

    @Override
    public LbResponse<ServiceInstance> choose(LbRequest<ServerWebExchange> request) {
        String serviceId = request.getContext().getAttribute(SERVICE_ID);
        List<ServiceInstance> selectInstances = registerCenter.selectInstances(serviceId);
        
        if (ObjectUtils.isEmpty(selectInstances)) {
            return null;
        }
        
        // 使用ThreadLocalRandom获得更好的性能
        int randomIndex = ThreadLocalRandom.current().nextInt(selectInstances.size());
        ServiceInstance instance = selectInstances.get(randomIndex);
        
        return new DefaultLbResponse(instance);
    }

    @Override
    public String loadBalanceType() {
        return "Random";
    }
} 