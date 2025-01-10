package com.muxin.gateway.core.loadbalance.register;

import java.util.Map;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/10 14:20
 */
public interface ServiceDefinition {

    String getServiceId();

    String getScheme();

    String getVersion();

    String scope();

    boolean enabled();

    Map<String, String> getMetadata();

}
