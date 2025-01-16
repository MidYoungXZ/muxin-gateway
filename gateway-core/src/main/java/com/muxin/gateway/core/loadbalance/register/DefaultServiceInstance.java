package com.muxin.gateway.core.loadbalance.register;

import lombok.Data;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/13 11:01
 */
@Data
public class DefaultServiceInstance implements ServiceInstance {

    private ServiceDefinition serviceDefinition;

    private String host;

    private int port;

    private boolean secure;

    private URI uri;

    public DefaultServiceInstance(ServiceDefinition serviceDefinition, String host, int port, boolean secure, URI uri) {
        this.serviceDefinition = serviceDefinition;
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.uri = uri;
    }


    @Override
    public ServiceDefinition serviceDefinition() {
        return serviceDefinition;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Data
    public static class DefaultServiceDefinition implements ServiceDefinition {

        private String serviceId;

        private String scheme;

        private String version;

        private String scope;

        private boolean enabled;

        private Map<String, String> metadata;

        public DefaultServiceDefinition(String serviceId, String scheme, String version, String scope, boolean enabled, Map<String, String> metadata) {
            this.serviceId = serviceId;
            this.scheme = scheme;
            this.version = version;
            this.scope = scope;
            this.enabled = enabled;
            this.metadata = metadata;
        }

        public DefaultServiceDefinition(String serviceId, String scheme, String version, String scope) {
            this.serviceId = serviceId;
            this.scheme = scheme;
            this.version = version;
            this.scope = scope;
            this.enabled = true;
            this.metadata = new LinkedHashMap<>();
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
        public String scope() {
            return scope;
        }

        @Override
        public boolean enabled() {
            return enabled;
        }

        @Override
        public Map<String, String> getMetadata() {
            return metadata;
        }
    }

}
