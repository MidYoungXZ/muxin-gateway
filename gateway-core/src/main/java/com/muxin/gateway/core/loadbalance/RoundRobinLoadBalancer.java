package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.loadbalance.register.RegisterCenter;
import com.muxin.gateway.core.loadbalance.register.ServiceInstance;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.muxin.gateway.core.common.GatewayConstants.SERVICE_ID;

/**
 * 实现了一个轮询（Round Robin）负载均衡器，继承自 {@link GatewayLoadBalance} 接口。
 * 该类根据传入的 {@link ServerWebExchange} 对象选择合适的服务实例，并返回负载均衡响应。
 *
 * @author Administrator
 * @date 2025/1/10 15:14
 */
public class RoundRobinLoadBalancer implements GatewayLoadBalance {

    final AtomicInteger position = new AtomicInteger(0);

    private final RegisterCenter registerCenter;

    /**
     * 构造函数，用于初始化轮询负载均衡器。
     *
     * @param registerCenter 注册中心，用于获取服务实例
     */
    public RoundRobinLoadBalancer(RegisterCenter registerCenter) {
        this.registerCenter = registerCenter;
    }

    /**
     * 根据负载均衡请求选择合适的服务实例，并返回负载均衡响应。
     *
     * @param request 负载均衡请求
     * @return 负载均衡响应
     */
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

    /**
     * 返回负载均衡的类型，固定为 "Round"。
     *
     * @return 负载均衡的类型 "Round"
     */
    @Override
    public String loadBalanceType() {
        return "Round";
    }
}
