package com.muxin.gateway.core.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认服务实例实现
 *
 * @author Administrator
 * @date 2025/6/13 18:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultServiceInstance implements ServiceInstance {

    private DefaultServiceDefinition serviceDefinition;
    private String instanceId;
    private String host;
    private int port;
    private boolean secure;
    private URI uri;
    private double weight = 1.0;
    private boolean healthy = true;
    private Map<String, String> metadata = new LinkedHashMap<>();

    @Override
    public ServiceDefinition getServiceDefinition() {
        return serviceDefinition;
    }

    @Override
    public String getInstanceId() {
        return instanceId != null ? instanceId : ServiceInstance.super.getInstanceId();
    }

    /**
     * 默认服务定义实现
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultServiceDefinition implements ServiceDefinition {

        private String serviceId;
        private String scheme;
        private String version;
        private String scope;
        private boolean enabled = true;
        private Map<String, String> metadata = new LinkedHashMap<>();
        private String description = "";
        private String environment = "prod";

        public DefaultServiceDefinition(String serviceId, String scheme, String version, String scope) {
            this.serviceId = serviceId;
            this.scheme = scheme;
            this.version = version;
            this.scope = scope;
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public String getScheme() {
            return scheme;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getScope() {
            return scope;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public Map<String, String> getMetadata() {
            return metadata;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getEnvironment() {
            return environment;
        }
    }
} 