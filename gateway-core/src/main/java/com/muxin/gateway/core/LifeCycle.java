package com.muxin.gateway.core;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/19 16:51
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
