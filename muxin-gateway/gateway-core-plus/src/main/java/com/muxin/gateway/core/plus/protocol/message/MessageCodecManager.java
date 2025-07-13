package com.muxin.gateway.core.plus.protocol.message;

import com.muxin.gateway.core.plus.common.LifeCycle;
import com.muxin.gateway.core.plus.common.Repository;

/**
 * @author: yangxz
 * @description:
 */
public interface MessageCodecManager extends Repository<Protocol, MessageCodec> , LifeCycle {


    /**
     * 检查是否支持指定的协议转换
     *
     * @param sourceProtocol 源协议
     * @return 是否支持转换
     */
    boolean supports(Protocol sourceProtocol);

}
