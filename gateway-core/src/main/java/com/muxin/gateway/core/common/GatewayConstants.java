package com.muxin.gateway.core.common;

import org.asynchttpclient.Response;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/22 16:23
 */
public final class GatewayConstants {

    private static String qualify(String attr) {
        return GatewayConstants.class.getName() + "." + attr;
    }

    /**
     * HTTP协议
     */
    public static final String HTTP = "http";
    /**
     * HTTPS协议
     */
    public static final String HTTPS = "https";

    public static final String BLANK_SEPARATOR = "";

    public static final String INTERNAL_STATUS = "internalStatus";

    /**
     * Gateway request URL attribute name.
     */
    public static final String GATEWAY_REQUEST_URL_ATTR = qualify("gatewayRequestUrl");

    public static final String SERVICE_ID = qualify("serviceId");

    public static final String META_DATA_KEY = qualify("meta");
    /**
     * Gateway original request URL attribute name.
     */
    public static final String GATEWAY_ORIGINAL_REQUEST_URL_ATTR = qualify("gatewayOriginalRequestUrl");

    /**
     * Gateway LoadBalancer {@link Response} attribute name.
     */
    public static final String GATEWAY_LOADBALANCER_RESPONSE_ATTR = qualify("gatewayLoadBalancerResponse");

    /**
     * GATEWAY2BACKEND_ORIGINAL_REQUEST_ATTR {@link Response} attribute name.
     */
    public static final String GATEWAY2BACKEND_ORIGINAL_REQUEST_ATTR = qualify("gateway2BackendOriginalRequest");

    /**
     * GATEWAY2BACKEND_ORIGINAL_RESPONSE_ATTR {@link Response} attribute name.
     */
    public static final String GATEWAY2BACKEND_ORIGINAL_RESPONSE_ATTR = qualify("gateway2BackendOriginalResponse");


}
