package com.muxin.gateway.core.common.exception;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 11:25
 */
public class GatewayException extends RuntimeException{

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
