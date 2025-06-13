package com.muxin.gateway.core.config;

import com.muxin.gateway.core.filter.FilterDefinition;
import com.muxin.gateway.core.route.RouteDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

import static com.muxin.gateway.core.common.GatewayConstants.GATEWAY_CONFIG_PREFIX;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/20 16:51
 */
@ConfigurationProperties(prefix = GATEWAY_CONFIG_PREFIX)
@Data
public class GatewayProperties {

    /**
     * Properties prefix.
     */
    public static final String PREFIX = GATEWAY_CONFIG_PREFIX;

    private List<RouteDefinition> routes = new ArrayList<>();

    private List<FilterDefinition> defaultFilters = new ArrayList<>();


}
