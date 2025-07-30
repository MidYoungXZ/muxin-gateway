package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.muxin.gateway.core.common.GatewayConstants.SERVICE_ID;

/**
 * 加权轮询负载均衡算法
 * 根据服务实例的权重进行轮询分配
 */
@Slf4j
public class WeightedRoundRobinLoadBalancer implements GatewayLoadBalance {

    private final RegisterCenter registerCenter;
    
    // 每个服务的权重状态
    private final Map<String, WeightedRoundRobin> weightMap = new ConcurrentHashMap<>();
    
    public WeightedRoundRobinLoadBalancer(RegisterCenter registerCenter) {
        this.registerCenter = registerCenter;
    }

    @Override
    public LbResponse<ServiceInstance> choose(LbRequest<ServerWebExchange> request) {
        String serviceId = request.getContext().getAttribute(SERVICE_ID);
        List<ServiceInstance> selectInstances = registerCenter.selectInstances(serviceId);
        
        if (ObjectUtils.isEmpty(selectInstances)) {
            return null;
        }
        
        // 获取或创建该服务的权重轮询对象
        WeightedRoundRobin wrr = weightMap.computeIfAbsent(serviceId, 
            k -> new WeightedRoundRobin(selectInstances));
        
        // 更新实例列表（处理实例变化的情况）
        wrr.updateInstances(selectInstances);
        
        // 选择实例
        ServiceInstance instance = wrr.select();
        
        return new DefaultLbResponse(instance);
    }

    @Override
    public String loadBalanceType() {
        return "WeightedRoundRobin";
    }
    
    /**
     * 加权轮询内部实现类
     */
    private static class WeightedRoundRobin {
        private List<ServiceInstance> instances;
        private int[] weights;
        private int[] currentWeights;
        private int totalWeight;
        
        public WeightedRoundRobin(List<ServiceInstance> instances) {
            updateInstances(instances);
        }
        
        public synchronized void updateInstances(List<ServiceInstance> newInstances) {
            this.instances = newInstances;
            this.weights = new int[instances.size()];
            this.currentWeights = new int[instances.size()];
            this.totalWeight = 0;
            
            for (int i = 0; i < instances.size(); i++) {
                // 从实例的metadata中获取权重，默认为1
                int weight = getWeight(instances.get(i));
                weights[i] = weight;
                currentWeights[i] = 0;
                totalWeight += weight;
            }
        }
        
        public synchronized ServiceInstance select() {
            if (instances.isEmpty()) {
                return null;
            }
            
            if (instances.size() == 1) {
                return instances.get(0);
            }
            
            int selectedIndex = -1;
            int maxCurrentWeight = Integer.MIN_VALUE;
            
            // 平滑加权轮询算法
            for (int i = 0; i < instances.size(); i++) {
                currentWeights[i] += weights[i];
                
                if (currentWeights[i] > maxCurrentWeight) {
                    maxCurrentWeight = currentWeights[i];
                    selectedIndex = i;
                }
            }
            
            if (selectedIndex >= 0) {
                currentWeights[selectedIndex] -= totalWeight;
                return instances.get(selectedIndex);
            }
            
            // 降级为简单轮询
            return instances.get(0);
        }
        
        private int getWeight(ServiceInstance instance) {
            // 从metadata中获取权重配置
            Map<String, String> metadata = instance.getMetadata();
            if (metadata != null && metadata.containsKey("weight")) {
                try {
                    return Integer.parseInt(metadata.get("weight"));
                } catch (NumberFormatException e) {
                    log.warn("Invalid weight value for instance {}: {}", 
                        instance.getInstanceId(), metadata.get("weight"));
                }
            }
            // 默认权重为1
            return 1;
        }
    }
} 