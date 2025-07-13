package com.muxin.gateway.core.plus.route.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认服务实例实现
 *
 * @author muxin
 */
@Slf4j
public class DefaultServiceInstance implements ServiceInstance {
    
    private final ServiceMeta gatewayService;
    private final String instanceId;
    private final EndpointAddress address;
    private final Map<String, Object> metadata;
    private volatile NodeStatus status;
    
    public DefaultServiceInstance(ServiceMeta gatewayService,
                                  String instanceId,
                                  EndpointAddress address) {
        this.gatewayService = Objects.requireNonNull(gatewayService, "gatewayService不能为空");
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId不能为空");
        this.address = Objects.requireNonNull(address, "address不能为空");
        this.metadata = new ConcurrentHashMap<>();
        this.status = NodeStatus.HEALTHY; // 默认健康状态
        
        log.debug("创建服务实例: {} - {}", instanceId, address.toUri());
    }
    
    public DefaultServiceInstance(ServiceMeta gatewayService,
                                  String instanceId,
                                  EndpointAddress address,
                                  NodeStatus status) {
        this(gatewayService, instanceId, address);
        this.status = status;
    }
    
    @Override
    public ServiceMeta service() {
        return gatewayService;
    }
    
    @Override
    public String instanceId() {
        return instanceId;
    }
    
    @Override
    public EndpointAddress getAddress() {
        return address;
    }
    
    @Override
    public NodeStatus getStatus() {
        return status;
    }
    
    @Override
    public void updateStatus(NodeStatus status) {
        NodeStatus oldStatus = this.status;
        this.status = status;
        log.debug("服务实例状态更新: {} {} -> {}", instanceId, oldStatus, status);
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public boolean isHealthy() {
        return status != null && status.isHealthy();
    }
    
    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * 获取元数据值
     */
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    /**
     * 检查是否可用
     */
    public boolean isAvailable() {
        return status != null && status.isAvailable();
    }
    
    /**
     * 获取实例描述
     */
    public String getDescription() {
        return String.format("ServiceInstance[%s/%s@%s:%s]", 
                gatewayService.getServiceName(), instanceId, 
                address.getHost(), address.getPort());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultServiceInstance that = (DefaultServiceInstance) o;
        return Objects.equals(instanceId, that.instanceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }
    
    @Override
    public String toString() {
        return String.format(
            "DefaultServiceInstance{service=%s, instanceId='%s', address=%s, status=%s, healthy=%s}",
            gatewayService.getServiceName(), instanceId, address.toUri(), status, isHealthy()
        );
    }
} 