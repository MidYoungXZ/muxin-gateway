package com.muxin.gateway.core.plus.route.service;

import com.muxin.gateway.core.plus.msg.Protocol;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认网关服务实现
 *
 * @author muxin
 */
public class DefaultServiceMeta implements ServiceMeta {
    
    private final String serviceId;
    private final String serviceName;
    private final String version;
    private final String description;
    private final Protocol protocol;
    private final Map<String, Object> metadata;
    
    public DefaultServiceMeta(String serviceId, String serviceName, Protocol protocol) {
        this(serviceId, serviceName, "1.0.0", "", protocol, new ConcurrentHashMap<>());
    }
    
    public DefaultServiceMeta(String serviceId, String serviceName, String version,
                              String description, Protocol protocol, Map<String, Object> metadata) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId不能为空");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName不能为空");
        this.version = version != null ? version : "1.0.0";
        this.description = description != null ? description : "";
        this.protocol = Objects.requireNonNull(protocol, "protocol不能为空");
        this.metadata = metadata != null ? metadata : new ConcurrentHashMap<>();
    }
    
    @Override
    public String getServiceId() {
        return serviceId;
    }
    
    @Override
    public String getServiceName() {
        return serviceName;
    }
    
    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public Protocol getProtocol() {
        return protocol;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultServiceMeta that = (DefaultServiceMeta) o;
        return Objects.equals(serviceId, that.serviceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }
    
    @Override
    public String toString() {
        return String.format("GatewayService{id='%s', name='%s', version='%s', protocol=%s}", 
                serviceId, serviceName, version, protocol.type());
    }
} 