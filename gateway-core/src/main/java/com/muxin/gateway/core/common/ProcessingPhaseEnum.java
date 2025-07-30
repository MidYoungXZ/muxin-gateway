package com.muxin.gateway.core.common;

/**
 * 标记请求的处理阶段
 *
 * @author Administrator
 * @date 2024/12/10 15:13
 */
public enum ProcessingPhaseEnum {

    /**
     * 一个请求正在执行中的状态
     */
    RUNNING(0),
    /**
     * 标志请求结束，写回Response
     */
    WRITTEN(1),
    /**
     * 写回成功后，设置该标识，如果是Netty ，ctx.WriteAndFlush(response)
     */
    COMPLETED(2),
    /**
     * 整个网关请求完毕，彻底结束
     */
    TERMINATED(-1);


    private final int phase;

    ProcessingPhaseEnum(int phase) {
        this.phase = phase;
    }

    public int getPhase() {
        return phase;
    }
}
