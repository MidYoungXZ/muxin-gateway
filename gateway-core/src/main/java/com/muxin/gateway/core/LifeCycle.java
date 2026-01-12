package com.muxin.gateway.core;

/**
 * 生命周期接口
 * 
 * 定义对象的初始化、启动和关闭生命周期方法
 *
 * @author Administrator
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
