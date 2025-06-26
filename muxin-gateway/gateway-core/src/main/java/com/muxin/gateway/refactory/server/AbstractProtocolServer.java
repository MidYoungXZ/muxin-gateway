package com.muxin.gateway.refactory.server;

import com.muxin.gateway.refactory.message.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 协议服务器抽象基类
 * 提供通用的服务器功能实现
 * 
 * @author muxin
 */
@Slf4j
public abstract class AbstractProtocolServer implements ProtocolServer {
    
    protected final Protocol protocol;
    protected final int port;
    protected final Map<String, Object> config;
    protected volatile boolean running = false;
    protected MessageHandler messageHandler;
    
    // 统计信息
    protected final AtomicLong totalConnections = new AtomicLong(0);
    protected final AtomicLong activeConnections = new AtomicLong(0);
    protected final AtomicLong totalMessages = new AtomicLong(0);
    protected final AtomicLong errorCount = new AtomicLong(0);
    protected final long startTime;
    
    protected AbstractProtocolServer(Protocol protocol, int port, Map<String, Object> config) {
        this.protocol = protocol;
        this.port = port;
        this.config = config != null ? new HashMap<>(config) : new HashMap<>();
        this.startTime = System.currentTimeMillis();
        
        log.info("[{}] 协议服务器创建 - 端口: {}, 协议: {}", 
            getClass().getSimpleName(), port, protocol.getName());
    }
    
    @Override
    public Protocol getSupportedProtocol() {
        return protocol;
    }
    
    @Override
    public int getPort() {
        return port;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public void bindMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
        log.info("[{}] 绑定消息处理器: {}", getClass().getSimpleName(), 
            handler != null ? handler.getClass().getSimpleName() : "null");
    }
    
    @Override
    public Map<String, Object> getServerStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("protocol", protocol.getName());
        stats.put("port", port);
        stats.put("running", running);
        stats.put("startTime", startTime);
        stats.put("uptime", System.currentTimeMillis() - startTime);
        stats.put("totalConnections", totalConnections.get());
        stats.put("activeConnections", activeConnections.get());
        stats.put("totalMessages", totalMessages.get());
        stats.put("errorCount", errorCount.get());
        return stats;
    }
    
    @Override
    public Map<String, Object> getServerConfig() {
        return new HashMap<>(config);
    }
    
    /**
     * 记录连接建立
     */
    public void recordConnectionEstablished() {
        totalConnections.incrementAndGet();
        activeConnections.incrementAndGet();
    }
    
    /**
     * 记录连接关闭
     */
    public void recordConnectionClosed() {
        activeConnections.decrementAndGet();
    }
    
    /**
     * 记录消息处理
     */
    public void recordMessage() {
        totalMessages.incrementAndGet();
    }
    
    /**
     * 记录错误
     */
    public void recordError() {
        errorCount.incrementAndGet();
    }
    
    /**
     * 获取配置值
     */
    protected <T> T getConfigValue(String key, T defaultValue) {
        Object value = config.get(key);
        if (value != null && defaultValue.getClass().isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }
    
    /**
     * 模板方法：子类实现具体的启动逻辑
     */
    protected abstract void doStart() throws Exception;
    
    /**
     * 模板方法：子类实现具体的停止逻辑
     */
    protected abstract void doStop() throws Exception;
    
    @Override
    public final void start() {
        if (running) {
            log.warn("[{}] 服务器已在运行中，忽略启动请求", getClass().getSimpleName());
            return;
        }
        
        try {
            log.info("[{}] 开始启动协议服务器 - 端口: {}", getClass().getSimpleName(), port);
            doStart();
            running = true;
            log.info("[{}] 协议服务器启动成功 - 端口: {}", getClass().getSimpleName(), port);
        } catch (Exception e) {
            log.error("[{}] 协议服务器启动失败 - 端口: {}", getClass().getSimpleName(), port, e);
            throw new RuntimeException("协议服务器启动失败", e);
        }
    }
    
    @Override
    public final void shutdown() {
        if (!running) {
            log.warn("[{}] 服务器未运行，忽略停止请求", getClass().getSimpleName());
            return;
        }
        
        try {
            log.info("[{}] 开始停止协议服务器 - 端口: {}", getClass().getSimpleName(), port);
            doStop();
            running = false;
            log.info("[{}] 协议服务器停止成功 - 端口: {}", getClass().getSimpleName(), port);
        } catch (Exception e) {
            log.error("[{}] 协议服务器停止失败 - 端口: {}", getClass().getSimpleName(), port, e);
        }
    }
} 