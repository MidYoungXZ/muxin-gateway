package com.muxin.gateway.core.common;


/**
 * 生命周期接口
 * 定义组件的生命周期方法，包括初始化、启动和关闭
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
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
