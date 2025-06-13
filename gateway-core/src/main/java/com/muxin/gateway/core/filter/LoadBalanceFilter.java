package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.loadbalance.DefaultLbRequest;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalance;
import com.muxin.gateway.core.loadbalance.GatewayLoadBalanceFactory;
import com.muxin.gateway.core.loadbalance.LbResponse;
import com.muxin.gateway.registry.api.ServiceInstance;
import com.muxin.gateway.core.utils.ExchangeUtil;

import java.net.URI;

import static com.muxin.gateway.core.common.GatewayConstants.GATEWAY_LOADBALANCER_RESPONSE_ATTR;
import static com.muxin.gateway.core.common.GatewayConstants.GATEWAY_REQUEST_URL_ATTR;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 16:45
 */
public class LoadBalanceFilter implements GlobalFilter {


    private GatewayLoadBalanceFactory gatewayLoadBalanceFactory;

    private String loadBalanceType = "Round";


    //定义Route包含Filter对象集合-》Filter持有GatewayLoadBalanceFactory-》
    // 根据参数获取LoadBalance单例对象

    @Override
    public void filter(ServerWebExchange exchange) {
        GatewayLoadBalance gatewayLoadBalance = gatewayLoadBalanceFactory.getGatewayLoadBalance(loadBalanceType);
        if (gatewayLoadBalance == null){
            throw new RuntimeException("loadBalanceType is not exist");
        }
        LbResponse<ServiceInstance> lbResponse = gatewayLoadBalance.choose(new DefaultLbRequest(exchange));
        if (!lbResponse.hasServer()){
            throw new RuntimeException("lbResponse is not exist");
        }
        URI requestUrl = ExchangeUtil.doReconstructURI(lbResponse.getServer(), URI.create(exchange.getRequest().uri()));
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
        exchange.getAttributes().put(GATEWAY_LOADBALANCER_RESPONSE_ATTR, lbResponse);

    }

    @Override
    public FilterTypeEnum filterType() {
        return FilterTypeEnum.REQUEST;
    }




    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 100;
    }
}
