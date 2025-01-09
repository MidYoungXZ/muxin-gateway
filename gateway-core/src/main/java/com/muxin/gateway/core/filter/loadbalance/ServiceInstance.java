package com.muxin.gateway.core.filter.loadbalance;

import java.net.URI;
import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 17:45
 */
public interface ServiceInstance {

    default String getInstanceId() {
        return null;
    }

    String getServiceId();

    String getHost();

    int getPort();

    boolean isSecure();

    URI getUri();

    Map<String, String> getMetadata();

    default String getScheme() {
        return null;
    }
}
