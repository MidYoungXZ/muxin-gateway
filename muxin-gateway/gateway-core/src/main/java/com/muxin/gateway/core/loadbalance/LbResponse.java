package com.muxin.gateway.core.loadbalance;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2025/1/9 17:25
 */
public interface LbResponse<T> {

    boolean hasServer();

    T getServer();

}
