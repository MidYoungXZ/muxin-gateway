package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.loadbalance.DefaultLbRequest;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalance;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalanceFactory;
import com.muxin.gateway.core.loadbalance.LbResponse;
import com.muxin.gateway.core.utils.ExchangeUtil;
import com.muxin.gateway.core.registry.ServiceInstance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import static com.muxin.gateway.core.common.GatewayConstants.*;


/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 16:45
 */
@Data
@Slf4j
public class LoadBalanceFilter implements GlobalFilter {

    private GatewayLoadBalanceFactory gatewayLoadBalanceFactory;

    private String loadBalanceType = "Round";

    //定义Route包含Filter对象集合-》Filter持有GatewayLoadBalanceFactory-》
    // 根据参数获取LoadBalance单例对象
    @Override
    public void filter(ServerWebExchange exchange) {
        // 检查是否设置了服务ID
        String serviceId = exchange.getAttribute(SERVICE_ID);
        if (serviceId == null || serviceId.isEmpty()) {
            log.warn("No service ID found in exchange attributes, skipping load balancing");
            return;
        }

        GatewayLoadBalance gatewayLoadBalance = gatewayLoadBalanceFactory.getGatewayLoadBalance(loadBalanceType);
        if (gatewayLoadBalance == null) {
            throw new RuntimeException("Load balance type '" + loadBalanceType + "' not found");
        }

        LbResponse<ServiceInstance> lbResponse = gatewayLoadBalance.choose(new DefaultLbRequest(exchange));
        if (lbResponse == null || !lbResponse.hasServer()) {
            throw new RuntimeException("No available service instance found for service: " + serviceId);
        }

        ServiceInstance instance = lbResponse.getServer();
        URI requestUrl = ExchangeUtil.doReconstructURI(instance, exchange);

        log.debug("Load balanced request to service: {}, instance: {}:{}", serviceId, instance.getHost(), instance.getPort());
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
        exchange.getAttributes().put(GATEWAY_LOADBALANCER_RESPONSE_ATTR, lbResponse);
    }


    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 100;
    }
}
