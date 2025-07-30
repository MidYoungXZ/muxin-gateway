package com.muxin.gateway.core.common;

/**
 * 标记请求处理的不同阶段
 *
 * @author Administrator
 * @date 2024/12/11 10:59
 */
public class ProcessingPhase {

    private ProcessingPhaseEnum processingPhase = ProcessingPhaseEnum.RUNNING;

    /**
     * 请求开始处理阶段
     * @return
     */
    public ProcessingPhase running() {
        processingPhase = ProcessingPhaseEnum.RUNNING;
        return this;
    }

    /**
     * 响应已经写入
     * @return
     */
    public ProcessingPhase written() {
        processingPhase = ProcessingPhaseEnum.WRITTEN;
        return this;
    }

    /**
     * 写回成功后，设置该标识，如果是Netty ，ctx.WriteAndFlush(response)
     * @return
     */
    public ProcessingPhase completed() {
        processingPhase = ProcessingPhaseEnum.COMPLETED;
        return this;
    }

    /**
     * 处理终止阶段
     * @return
     */
    public ProcessingPhase terminated() {
        processingPhase = ProcessingPhaseEnum.TERMINATED;
        return this;
    }


    public boolean isRunning() {
        return processingPhase == ProcessingPhaseEnum.RUNNING;
    }


    public boolean isWritten() {
        return processingPhase == ProcessingPhaseEnum.WRITTEN;
    }


    public boolean isCompleted() {
        return processingPhase == ProcessingPhaseEnum.COMPLETED;
    }


    public boolean isTerminated() {
        return processingPhase == ProcessingPhaseEnum.TERMINATED;
    }
}
