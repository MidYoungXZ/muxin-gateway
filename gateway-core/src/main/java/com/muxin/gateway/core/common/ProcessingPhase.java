package com.muxin.gateway.core.common;

/**
 * 标记请求处理的不同阶段
 *
 * @author Administrator
 * @date 2024/12/11 10:59
 */
public class ProcessingPhase {

    private ProcessingPhaseEnum processingPhase = ProcessingPhaseEnum.RUNNING;


    public ProcessingPhase running() {
        processingPhase = ProcessingPhaseEnum.RUNNING;
        return this;
    }


    public ProcessingPhase written() {
        processingPhase = ProcessingPhaseEnum.WRITTEN;
        return this;
    }


    public ProcessingPhase completed() {
        processingPhase = ProcessingPhaseEnum.COMPLETED;
        return this;
    }


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
