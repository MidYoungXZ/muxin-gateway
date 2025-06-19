package com.muxin.gateway.core.loadbalance;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 17:21
 */
public interface LoadBalance<S, C> {

    LbResponse<S> choose(LbRequest<C> request);

}
