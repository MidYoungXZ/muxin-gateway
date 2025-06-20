package com.muxin.gateway.refactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 轮询负载均衡策略
 *
 * @author muxin
 */
public class RoundRobinLoadBalancer implements LoadBalanceStrategy {
    
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final Map<String, Boolean> healthStatus = new ConcurrentHashMap<>();
    
    @Override
    public EndpointAddress select(List<EndpointAddress> availableAddresses, UniversalRequestContext context) {
        if (availableAddresses == null || availableAddresses.isEmpty()) {
            return null;
        }
        
        // 过滤健康的地址
        List<EndpointAddress> healthyAddresses = availableAddresses.stream()
                .filter(addr -> isHealthy(addr))
                .collect(Collectors.toList());
        
        if (healthyAddresses.isEmpty()) {
            // 如果没有健康的地址，使用所有地址
            healthyAddresses = availableAddresses;
        }
        
        // 轮询选择
        int index = currentIndex.getAndIncrement() % healthyAddresses.size();
        EndpointAddress selected = healthyAddresses.get(index);
        
        System.out.println(String.format("[LOAD_BALANCER] RoundRobin selected: %s (index=%d/%d)", 
            selected.toUri(), index, healthyAddresses.size()));
        
        return selected;
    }
    
    @Override
    public String getName() {
        return "ROUND_ROBIN";
    }
    
    @Override
    public void updateHealthStatus(EndpointAddress address, boolean isHealthy) {
        healthStatus.put(address.toUri(), isHealthy);
        System.out.println(String.format("[LOAD_BALANCER] Health status updated: %s -> %s", 
            address.toUri(), isHealthy ? "HEALTHY" : "UNHEALTHY"));
    }
    
    @Override
    public Object getConfiguration() {
        return Map.of(
            "strategy", "ROUND_ROBIN",
            "currentIndex", currentIndex.get(),
            "healthyCount", (int) healthStatus.values().stream().mapToLong(h -> h ? 1 : 0).sum()
        );
    }
    
    private boolean isHealthy(EndpointAddress address) {
        return healthStatus.getOrDefault(address.toUri(), true); // 默认认为是健康的
    }
} 