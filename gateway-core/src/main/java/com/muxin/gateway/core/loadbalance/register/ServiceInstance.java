package com.muxin.gateway.core.loadbalance.register;

import java.net.URI;
import java.util.UUID;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 17:45
 */
public interface ServiceInstance {

    ServiceDefinition serviceDefinition();

    default String getInstanceId() {
        return UUID.randomUUID().toString();
    }

    String getHost();

    int getPort();

    boolean isSecure();

    URI getUri();

}
