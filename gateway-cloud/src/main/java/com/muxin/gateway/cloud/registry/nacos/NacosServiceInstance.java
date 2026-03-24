package com.muxin.gateway.cloud.registry.nacos;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.muxin.gateway.core.service.InstanceSource;
import com.muxin.gateway.core.service.ServiceInstance;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class NacosServiceInstance implements ServiceInstance {

    private final Instance nacosInstance;
    private final String serviceId;
    private final String instanceId;

    public NacosServiceInstance(String serviceId, Instance instance) {
        this.serviceId = serviceId;
        this.nacosInstance = instance;
        this.instanceId = instance.getInstanceId();
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return nacosInstance.getIp();
    }

    @Override
    public int getPort() {
        return nacosInstance.getPort();
    }

    @Override
    public String getScheme() {
        Map<String, String> metadata = getMetadata();
        String scheme = metadata.get("scheme");
        if (scheme != null && !scheme.isEmpty()) {
            return scheme;
        }
        return nacosInstance.getMetadata().getOrDefault("secure", "false").equals("true") ? "https" : "http";
    }

    @Override
    public double getWeight() {
        return nacosInstance.getWeight();
    }

    @Override
    public boolean isHealthy() {
        return nacosInstance.isHealthy() && nacosInstance.isEnabled();
    }

    @Override
    public InstanceSource getSource() {
        return InstanceSource.DISCOVERY;
    }

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<>(nacosInstance.getMetadata());
        String clusterName = nacosInstance.getClusterName();
        if (clusterName != null && !clusterName.isEmpty()) {
            metadata.put("nacos.cluster", clusterName);
        }
        metadata.put("nacos.ephemeral", String.valueOf(nacosInstance.isEphemeral()));
        metadata.put("nacos.enabled", String.valueOf(nacosInstance.isEnabled()));
        return metadata;
    }

    public String getClusterName() {
        return nacosInstance.getClusterName();
    }

    public boolean isEphemeral() {
        return nacosInstance.isEphemeral();
    }

    public Instance getNacosInstance() {
        return nacosInstance;
    }

    @Override
    public String toString() {
        return String.format("NacosServiceInstance{serviceId='%s', instanceId='%s', host='%s', port=%d, healthy=%s, weight=%.1f}",
                serviceId, instanceId, getHost(), getPort(), isHealthy(), getWeight());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NacosServiceInstance that = (NacosServiceInstance) o;
        return instanceId.equals(that.instanceId);
    }

    @Override
    public int hashCode() {
        return instanceId.hashCode();
    }
}