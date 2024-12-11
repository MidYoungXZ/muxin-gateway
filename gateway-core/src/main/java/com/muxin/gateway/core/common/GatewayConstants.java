package com.muxin.gateway.core.common;

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


    /**
     * Gateway request URL attribute name.
     */
    public static final String GATEWAY_REQUEST_URL_ATTR = qualify("gatewayRequestUrl");




}
