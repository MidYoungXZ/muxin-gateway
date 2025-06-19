package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.muxin.gateway.core.common.GatewayConstants.SERVICE_ID;

/**
 * 最少连接负载均衡算法
 * 选择当前连接数最少的服务实例
 */
@Slf4j
public class LeastConnectionsLoadBalancer implements GatewayLoadBalance {

    private final RegisterCenter registerCenter;
    
    // 记录每个实例的活跃连接数
    private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    
    public LeastConnectionsLoadBalancer(RegisterCenter registerCenter) {
        this.registerCenter = registerCenter;
    }

    @Override
    public LbResponse<ServiceInstance> choose(LbRequest<ServerWebExchange> request) {
        String serviceId = request.getContext().getAttribute(SERVICE_ID);
        List<ServiceInstance> selectInstances = registerCenter.selectInstances(serviceId);
        
        if (ObjectUtils.isEmpty(selectInstances)) {
            return null;
        }
        
        if (selectInstances.size() == 1) {
            return new DefaultLbResponse(selectInstances.get(0));
        }
        
        // 选择连接数最少的实例
        ServiceInstance selectedInstance = null;
        int minConnections = Integer.MAX_VALUE;
        
        for (ServiceInstance instance : selectInstances) {
            String instanceKey = getInstanceKey(instance);
            AtomicInteger connections = connectionCounts.computeIfAbsent(instanceKey, 
                k -> new AtomicInteger(0));
            
            int currentConnections = connections.get();
            if (currentConnections < minConnections) {
                minConnections = currentConnections;
                selectedInstance = instance;
            }
        }
        
        if (selectedInstance != null) {
            // 增加选中实例的连接数
            String selectedKey = getInstanceKey(selectedInstance);
            connectionCounts.get(selectedKey).incrementAndGet();
            
            // 在响应完成后需要减少连接数
            // 这里我们将选中的实例key存储在exchange中，以便后续处理
            request.getContext().getAttributes().put("lb.instance.key", selectedKey);
            
            return new LeastConnectionsResponse(selectedInstance, this);
        }
        
        // 降级为选择第一个实例
        return new DefaultLbResponse(selectInstances.get(0));
    }

    @Override
    public String loadBalanceType() {
        return "LeastConnections";
    }
    
    /**
     * 减少实例的连接数（在请求完成后调用）
     */
    public void releaseConnection(String instanceKey) {
        AtomicInteger connections = connectionCounts.get(instanceKey);
        if (connections != null) {
            connections.decrementAndGet();
        }
    }
    
    /**
     * 清理不再使用的实例连接计数
     */
    public void cleanupStaleInstances(List<ServiceInstance> activeInstances) {
        // 构建活跃实例的key集合
        Map<String, Boolean> activeKeys = new ConcurrentHashMap<>();
        for (ServiceInstance instance : activeInstances) {
            activeKeys.put(getInstanceKey(instance), true);
        }
        
        // 移除不再活跃的实例
        connectionCounts.entrySet().removeIf(entry -> 
            !activeKeys.containsKey(entry.getKey()) && entry.getValue().get() == 0);
    }
    
    private String getInstanceKey(ServiceInstance instance) {
        return instance.getServiceDefinition().getServiceId() + ":" + instance.getHost() + ":" + instance.getPort();
    }
    
    /**
     * 自定义响应类，支持连接释放
     */
    public static class LeastConnectionsResponse extends DefaultLbResponse {
        private final LeastConnectionsLoadBalancer balancer;
        
        public LeastConnectionsResponse(ServiceInstance instance, 
                                      LeastConnectionsLoadBalancer balancer) {
            super(instance);
            this.balancer = balancer;
        }
        
        public void releaseConnection() {
            if (getServer() != null) {
                String instanceKey = balancer.getInstanceKey(getServer());
                balancer.releaseConnection(instanceKey);
            }
        }
    }
} 