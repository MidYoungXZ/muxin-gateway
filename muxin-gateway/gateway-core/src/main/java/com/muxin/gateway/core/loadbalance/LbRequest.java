package com.muxin.gateway.core.loadbalance;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 17:26
 */
public interface LbRequest<C> {

    default C getContext() {
        return null;
    }

}
