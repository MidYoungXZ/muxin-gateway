package com.muxin.gateway.core.plus.protocol.message;

import lombok.Data;

/**
 * @author Administrator
 * @since 1.0
 */
@Data
public class ProtocolData {

    private Protocol protocol;

    private Object data;

    public ProtocolData(Protocol protocol, Object data) {
        this.protocol = protocol;
        this.data = data;
    }
}
