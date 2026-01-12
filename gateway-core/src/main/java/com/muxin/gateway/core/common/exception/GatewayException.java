package com.muxin.gateway.core.common.exception;

/**
 * 网关异常类
 * 
 * 网关系统运行时抛出的自定义异常
 *
 * @author Administrator
 * @since 1.0.0
 */
public class GatewayException extends RuntimeException{

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
