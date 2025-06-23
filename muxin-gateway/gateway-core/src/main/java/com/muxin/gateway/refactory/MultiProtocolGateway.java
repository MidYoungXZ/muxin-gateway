package com.muxin.gateway.refactory;

import com.muxin.gateway.refactory.connect.Connection;
import com.muxin.gateway.refactory.message.Message;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 多协议网关接口
 *
 * @author muxin
 */
public interface MultiProtocolGateway {
    
    /**
     * 处理入站消息
     */
    CompletableFuture<Message> handleInbound(Message inboundMessage, Connection inboundConnection);
    
    /**
     * 支持的协议
     */
    Set<Protocol> getSupportedProtocols();
    
    /**
     * 注册协议适配器
     */
    void registerProtocolAdapter(ProtocolAdapter adapter);
    
    /**
     * 启动协议监听器
     */
    void startProtocolListener(Protocol protocol, int port, Map<String, Object> config);
    
    /**
     * 停止协议监听器
     */
    void stopProtocolListener(Protocol protocol);
    
    /**
     * 获取协议统计信息
     */
    Map<Protocol, Object> getProtocolStats();
    
    /**
     * 网关启动
     */
    void start();
    
    /**
     * 网关停止
     */
    void stop();
    
    /**
     * 网关状态
     */
    boolean isRunning();
} 