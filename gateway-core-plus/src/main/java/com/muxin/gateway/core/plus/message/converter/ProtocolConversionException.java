package com.muxin.gateway.core.plus.message.converter;

/**
 * 协议转换异常
 *
 * @author muxin
 * @since 1.0.0
 */
public class ProtocolConversionException extends RuntimeException {

    private final String fromProtocol;
    private final String toProtocol;

    public ProtocolConversionException(String message, String fromProtocol, String toProtocol) {
        super(String.format("协议转换失败 [%s -> %s]: %s", fromProtocol, toProtocol, message));
        this.fromProtocol = fromProtocol;
        this.toProtocol = toProtocol;
    }

    public ProtocolConversionException(String message, String fromProtocol, String toProtocol, Throwable cause) {
        super(String.format("协议转换失败 [%s -> %s]: %s", fromProtocol, toProtocol, message), cause);
        this.fromProtocol = fromProtocol;
        this.toProtocol = toProtocol;
    }

    public String getFromProtocol() {
        return fromProtocol;
    }

    public String getToProtocol() {
        return toProtocol;
    }
}
