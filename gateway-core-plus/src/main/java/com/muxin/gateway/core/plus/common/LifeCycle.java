package com.muxin.gateway.core.plus.common;


/**
 * @author muxin
 * @since 1.0
 */
public interface LifeCycle {

    /**
     * 初始化
     */
    void init();

    /**
     * 启动
     */
    void start();

    /**
     * 关闭
     */
    void shutdown();
}
