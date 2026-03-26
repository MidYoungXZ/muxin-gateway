package com.muxin.gateway.cloud.discovery;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "muxin.gateway.cloud")
public class CloudProperties {

    private String discovery = "nacos";

    private String config = "nacos";

    public boolean isNacosDiscovery() {
        return "nacos".equalsIgnoreCase(discovery);
    }

    public boolean isNacosConfig() {
        return "nacos".equalsIgnoreCase(config);
    }
}