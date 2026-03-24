package com.muxin.gateway.config;

import com.muxin.gateway.core.GatewayBootstrap;
import lombok.Getter;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class GatewayBootstrapWrapper extends GatewayBootstrap implements SmartLifecycle {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            init();
            super.start();
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 1000;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}