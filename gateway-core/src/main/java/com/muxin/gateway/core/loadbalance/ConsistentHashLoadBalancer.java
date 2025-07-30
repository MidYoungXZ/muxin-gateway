package com.muxin.gateway.core.loadbalance;

import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.RegisterCenter;
import com.muxin.gateway.core.registry.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.muxin.gateway.core.common.GatewayConstants.SERVICE_ID;

/**
 * 一致性哈希负载均衡算法
 * 基于请求特征（如客户端IP、请求路径等）进行哈希，保证相同特征的请求总是路由到同一个服务实例
 */
@Slf4j
public class ConsistentHashLoadBalancer implements GatewayLoadBalance {

    private final RegisterCenter registerCenter;
    
    // 每个服务的哈希环
    private final Map<String, ConsistentHashRing> hashRings = new ConcurrentHashMap<>();
    
    // 虚拟节点倍数
    private static final int VIRTUAL_NODE_FACTOR = 150;
    
    public ConsistentHashLoadBalancer(RegisterCenter registerCenter) {
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
        
        // 获取或创建该服务的哈希环
        ConsistentHashRing hashRing = hashRings.computeIfAbsent(serviceId, 
            k -> new ConsistentHashRing(VIRTUAL_NODE_FACTOR));
        
        // 更新哈希环中的节点
        hashRing.updateNodes(selectInstances);
        
        // 获取请求的哈希key
        String hashKey = getHashKey(request.getContext());
        
        // 根据哈希key选择实例
        ServiceInstance instance = hashRing.getNode(hashKey);
        
        return new DefaultLbResponse(instance);
    }

    @Override
    public String loadBalanceType() {
        return "ConsistentHash";
    }
    
    /**
     * 获取请求的哈希key
     * 可以基于客户端IP、请求路径、请求参数等
     */
    private String getHashKey(ServerWebExchange exchange) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // 1. 优先使用客户端IP
        SocketAddress remoteSocketAddr = exchange.remoteAddress();
        String clientIp = remoteSocketAddr != null ? remoteSocketAddr.toString() : null;
        if (clientIp != null && !clientIp.isEmpty()) {
            keyBuilder.append(clientIp);
        }
        
        // 2. 添加请求路径
        String path = exchange.fullPath();
        if (path != null) {
            keyBuilder.append(":").append(path);
        }
        
        // 3. 可选：添加特定的请求头或参数作为哈希因子
        String sessionId = exchange.getRequest().requestHeaders().get("X-Session-Id");
        if (sessionId != null) {
            keyBuilder.append(":").append(sessionId);
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 一致性哈希环实现
     */
    private static class ConsistentHashRing {
        private final TreeMap<Long, ServiceInstance> ring = new TreeMap<>();
        private final int virtualNodeFactor;
        private List<ServiceInstance> currentNodes = new ArrayList<>();
        
        public ConsistentHashRing(int virtualNodeFactor) {
            this.virtualNodeFactor = virtualNodeFactor;
        }
        
        /**
         * 更新哈希环中的节点
         */
        public synchronized void updateNodes(List<ServiceInstance> newNodes) {
            // 如果节点列表没有变化，不需要更新
            if (isSameNodes(currentNodes, newNodes)) {
                return;
            }
            
            // 清空现有环
            ring.clear();
            
            // 添加新节点
            for (ServiceInstance instance : newNodes) {
                addNode(instance);
            }
            
            currentNodes = new ArrayList<>(newNodes);
            log.debug("Updated hash ring with {} nodes", newNodes.size());
        }
        
        /**
         * 添加节点到哈希环
         */
        private void addNode(ServiceInstance instance) {
            String nodeKey = getNodeKey(instance);
            
            // 添加虚拟节点
            for (int i = 0; i < virtualNodeFactor; i++) {
                String virtualKey = nodeKey + "#" + i;
                long hash = hash(virtualKey);
                ring.put(hash, instance);
            }
        }
        
        /**
         * 根据key获取对应的节点
         */
        public ServiceInstance getNode(String key) {
            if (ring.isEmpty()) {
                return null;
            }
            
            long hash = hash(key);
            
            // 获取大于等于该hash值的第一个节点
            Map.Entry<Long, ServiceInstance> entry = ring.ceilingEntry(hash);
            
            // 如果没有找到，则返回第一个节点（环形结构）
            if (entry == null) {
                entry = ring.firstEntry();
            }
            
            return entry.getValue();
        }
        
        /**
         * 获取节点的唯一标识
         */
        private String getNodeKey(ServiceInstance instance) {
            return instance.getHost() + ":" + instance.getPort();
        }
        
        /**
         * 判断节点列表是否相同
         */
        private boolean isSameNodes(List<ServiceInstance> nodes1, List<ServiceInstance> nodes2) {
            if (nodes1.size() != nodes2.size()) {
                return false;
            }
            
            Set<String> keys1 = new HashSet<>();
            for (ServiceInstance node : nodes1) {
                keys1.add(getNodeKey(node));
            }
            
            Set<String> keys2 = new HashSet<>();
            for (ServiceInstance node : nodes2) {
                keys2.add(getNodeKey(node));
            }
            
            return keys1.equals(keys2);
        }
        
        /**
         * 使用MD5计算哈希值
         */
        private long hash(String key) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
                
                // 取前8个字节作为long值
                long hash = 0;
                for (int i = 0; i < 8; i++) {
                    hash = (hash << 8) | (digest[i] & 0xFF);
                }
                
                return hash;
            } catch (NoSuchAlgorithmException e) {
                // 降级为简单哈希
                return key.hashCode();
            }
        }
    }
} 