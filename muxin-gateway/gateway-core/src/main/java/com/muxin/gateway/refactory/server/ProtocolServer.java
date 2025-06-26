package com.muxin.gateway.refactory.server;

import com.muxin.gateway.refactory.LifeCycle;
import com.muxin.gateway.refactory.message.Protocol;

import java.util.Map;

/**
 * 协议服务器抽象接口
 * 定义所有协议服务器的通用能力和生命周期管理
 * 
 * @author muxin
 */
public interface ProtocolServer extends LifeCycle {
    
    /**
     * 获取支持的协议
     */
    Protocol getSupportedProtocol();
    
    /**
     * 获取监听端口
     */
    int getPort();
    
    /**
     * 服务器运行状态
     */
    boolean isRunning();
    
    /**
     * 绑定消息处理器
     */
    void bindMessageHandler(MessageHandler handler);
    
    /**
     * 获取服务器统计信息
     */
    Map<String, Object> getServerStats();
    
    /**
     * 获取服务器配置
     */
    Map<String, Object> getServerConfig();
} 