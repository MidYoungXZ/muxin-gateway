package com.muxin.gateway.core.common.exception;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/17 14:09
 */
public class FilterException extends GatewayException{


    public FilterException(String message) {
        super(message);
    }

    public FilterException(String message, Throwable cause) {
        super(message, cause);
    }
}
